package com.mugunga.counterpoint;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.PatternProducer;

/**
 * In musical terms, a Cantus Firmus is a well formed musical line or melody. The Cantus Firmus the initial or base 
 * melody created by the algorithm. Since a Cantus Firmus can't not be a musical melody, it extends the Note Melody class.
 * 
 * The Cantus Firmus is only instantiated when a valid melody is found by a Species Builder. Therefore, you
 * never have a partial melody or working melody, but only a finalized and valid Cantus Firmus in this class.
 * 
 * The Cantus Firmus is also the controller for generating its own child melodies, so it is a powerful class.
 * 
 * @author laurencemarrin
 *
 */
public class CantusFirmus extends NoteMelody {
	
	private boolean logging = true;
	
	private final  String MIDIdirectory = "MidiFiles/";
	private final CounterPointUtility mUtility = new CounterPointUtility();
	
	private Pattern cantusFirmusPattern;
	private List<FirstSpecies> firstSpeciesList = new ArrayList<>();
	private String cfMIDIpattern = "";
	private List<String> firstSpeciesPatternStrings = new ArrayList<>();
	public List<Pattern> firstSpeciesMIDIPatterns = new ArrayList<>();
	public List<Pattern> secondSpeciesMIDIPatterns = new ArrayList<>();
	private static List<SpeciesBuilder> buildChain = new ArrayList<>();
	
	private int dbID;
	
	/**
	 * Class constructor.
	 * @param sb Only create a Cantus Firmus when the Species Builder has a final and valid melody
	 * @param no1S Determines whether we will create firstSpecies. 
	 */
	public CantusFirmus (SpeciesBuilder sb, boolean no1S) {
		super(sb.getMelody());
		this.tailorStepIndexes();
		setPattern();
	}
	/**
	 * Create a SpeciesBuilder, send in ourself as the parent melody, and specify which type of 
	 * child melody we will generate. 
	 * 
	 * @param speciesType
	 */
	public void generateSpecies(SpeciesType speciesType) {

		SpeciesBuilder speciesZero = new SpeciesBuilder(this, speciesType);
		for(int i : speciesZero.getValidNextIndexesRandomized()) {
			SpeciesBuilder childSpecies = new SpeciesBuilder(speciesZero);
			if(childSpecies.checkAndSetFirstNote(i)) {
				buildChain.add(childSpecies);
				recursiveMelodySequencer(buildChain);				
			}
		}
		log("First Species Count: " + firstSpeciesList.size());
	}
	
	/**
	 * Recursively test potential notes to add to the melodies we are building. Each time a valid
	 * child melody is found, it is logged and stored, and recursion continues to find all valid 
	 * child melodies. In the end, there is a list of all valid child melodies of this species type in
	 * the parent melody. 
	 * 
	 * @param buildChain
	 */
	private void recursiveMelodySequencer(List<SpeciesBuilder> buildChain) {
		
		SpeciesBuilder currentSB = buildChain.get(buildChain.size()-1);
		List<Integer> nextValidIndexes = currentSB.getNextValidIndexArrayRandomized();
		for (int i : nextValidIndexes) {
			if (currentSB.testAsNextIndex(i)) {
				SpeciesBuilder newSB = new SpeciesBuilder(currentSB);
				if (newSB.addIntervalAndCheckForCompletion(newSB.nextInterval)) {
					logFirstSpecies(newSB);
						
				} else {
					buildChain.add(newSB);
					recursiveMelodySequencer(buildChain);
				}
			}
		}
		buildChain.remove(buildChain.size() - 1);
	}	
	/**
	 * When a first species is found, create a new instance of FirstSpecies from the completed SpeciesBuilder, add it 
	 * to our list, and append the new child melody to the MIDI string. 
	 * @param newCFB
	 */
	private void logFirstSpecies(SpeciesBuilder newCFB) {
		firstSpeciesList.add(new FirstSpecies(newCFB.getMelody()));
		String patternString = mUtility.getMIDIString(this.getLastFirstSpecies(), getMode(), mUtility.melodyStartIndex);
		firstSpeciesPatternStrings.add(patternString);
		firstSpeciesMIDIPatterns.add(new Pattern(patternString));
	}
	/**
	 * The master MIDI file contains the parent melody, as well as each child melody generated, overlay upon it's child melody
	 * 
	 * @param prefix
	 */
	private void writeMasterMIDIFile(String prefix) {
		String masterMIDIPattern = cfMIDIpattern + "R ";
		
		for(Pattern p : firstSpeciesMIDIPatterns) {
			masterMIDIPattern += p + "R ";
		}
		
		Pattern masterPattern = new Pattern(masterMIDIPattern);
		File file = new File(MIDIdirectory + prefix + cantusFirmusPattern.toString() + ".mid" );
		try {
			MidiFileManager.savePatternToMidi((PatternProducer) masterPattern, file);
		} catch (Exception ex) {
			ex.getStackTrace();
		}
	}
	
	private void setPattern() {
		this.cfMIDIpattern = mUtility.getMIDIString(this, getMode(), mUtility.melodyStartIndex);
		this.cantusFirmusPattern = mUtility.getMIDIPattern(this, getMode(), mUtility.melodyStartIndex);
	}

	public void createMIDIfile(String directory, String filenamePrefix) {
		writeMasterMIDIFile(filenamePrefix);	
	}

	public NoteMelody getLastFirstSpecies() {
		return firstSpeciesList.get(firstSpeciesList.size() - 1);
	}
	
	private void log(String msg) {
		if(logging) {
			System.out.println("CantusFirmusLog:      " + msg);
		}
	}

	public void setdbID(int dbID) {
		this.dbID = dbID;
	}

	public List<FirstSpecies> getFirstSpeciesList() {
		return firstSpeciesList;
	}

	public int getDBid() {
		return dbID;
	}

	public int getfirstSpeciesCount() {
		return firstSpeciesList.size();
	}
	
}
