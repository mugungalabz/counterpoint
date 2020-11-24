package com.mugunga.counterpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mugunga.musicmodels.Mode;
import com.mugunga.musicmodels.NoteIndexCollection;
import com.mugunga.musicmodels.NoteLength;
/**
 * 
 * The Species Builder encapsulates the melody that is in progress at each step, and maintains
 * the various rules we are following, and tracks a list of which notes might be valid next notes. 
 * 
 * 'Melody' and 'Species' can often be used interchangeably, and so one could also think of this
 * class as a 'Melody' builder. However, a specfic set of rule driving the algorithm is what determines
 * the species type (Cantus Firmus = base melody, First Species = 1:1 countermelody). Therefore
 * The term 'SpeciesBuilder' is more specific than 'MelodyBuilder'.
 * 
 * @author laurencemarrin
 *
 */
public class SpeciesBuilder {
	//public boolean logTestString = false;
	public boolean logginOn = false;
	public boolean logCF = false;
	public boolean testingAMelody = false;
	
	private SpeciesRules rules;
	private double maxMeasures;
	private int maxNotes;
	private double minMeasures;
	private int minNotes;

	private NoteIndexCollection validNextIndexes;
	private NoteIndexCollection validNextIndexesSaved;
	private int lastIndex = 0;
	private int lastInterval = 0;
	private Map<Integer, Integer> indexMap = new HashMap<>();
	private NoteMelodyInProgress noteMelody;
	
	//Test Fields used to vet whether the current note being tested will be a valid addition to the melody
	private int testInterval;
	private int testStepInterval;
	private int testIndex;
	private int testStepIndex;
	private int testMotionType;
	
	public int nextInterval;
	private int nextMotion;
	public Map <Integer, NoteIndexCollection> validIndexesMap;
	
	/**
	 * Class Constructor specifying the scale (Mode), the SpeciesType (Primary or Counter Melody,
	 * and feeding a TestMelody. TestMelody is left null if we want to generate random melodies
	 * 
	 * @param mode
	 * @param speciesType
	 * @param testMelody
	 */
	public SpeciesBuilder(Mode mode, 
						SpeciesType speciesType, 
						TestMelody testMelody) {
		
		rules = new SpeciesRules(speciesType);
		noteMelody = new NoteMelodyInProgress(rules, mode);
		maxMeasures = rules.maxMeasures;
		minMeasures = rules.minMeasures;
		
		List<Integer> startIdxList = new ArrayList<>();
		if(null == testMelody) {
			log("ASDF adding valid StartIndex");
			for(int i : rules.validStartIndexes ) {
				log("valid start index: " + i);
				startIdxList.add(i);
			}
			
		} else {
			testingAMelody = true;
			noteMelody.setTestMelody(testMelody);
			minMeasures = testMelody.melodyLength();
			maxMeasures = testMelody.melodyLength();
			startIdxList.add(testMelody.getFirst());
		}
		
		validIndexesMap = new HashMap <>();
		validIndexesMap.put(1, new NoteIndexCollection(startIdxList) );
//		log("validIndexesMap" + validIndexesMap);
		validNextIndexes = validIndexesMap.get(1);
		lastIndex = 0;
	}
	
	/**
	 * Constructor called when we have a parent melody (base melody or Cantus Firmus). It actually 
	 * sends itself into the SpeciesBuilder and specifies which type of melody we want to build.
	 * 
	 * If we are testing a predetermined child melody, we set the only valid next index to the first
	 * note in that melody. 
	 * 
	 * @param cantusFirmus The Parent Melody
	 * @param speciesType The type of countermelody we are creating for the Parent Melody
	 * 
	 */
	public SpeciesBuilder(CantusFirmus cantusFirmus, SpeciesType speciesType) {
//		log("Species Builder Constructor from first Species");
		rules =  new SpeciesRules(speciesType);
		noteMelody = new NoteMelodyInProgress(rules, cantusFirmus.getMode());
		noteMelody.setParentMelody(cantusFirmus);
		validNextIndexes = new NoteIndexCollection();
		if(cantusFirmus.isTestingChildSpecies()) {
			log(" we are testing a child species?");
			testingAMelody = true;
			noteMelody.setTestMelody(cantusFirmus.getChildSpeciesTestMelody());
			minMeasures = noteMelody.testMelodyLength();
			maxMeasures = noteMelody.testMelodyLength();
			validNextIndexes.add(cantusFirmus.getChildSpeciesTestMelody().get(0));
		} else {
			for (int i : rules.speciesType.validStartIndexes) {
				validNextIndexes.add(i);
			}			
		}
		maxMeasures = cantusFirmus.size();
		minMeasures = cantusFirmus.size();
	}

	/**
	 * This is a deep clone constructor. It is used when recursively adding notes to a melody
	 * so we don't have to undo the changes made if we go back and explore an earlier fragment 
	 * of the melody.
	 * 
	 * @param o The original SpeciesBuilder to clone
	 */
	public SpeciesBuilder(SpeciesBuilder o) {
		this.rules = o.rules;
		this.noteMelody = new NoteMelodyInProgress(o.noteMelody);
		this.noteMelody.setTestMelody(o.getMelody().getTestMelody());
		this.testingAMelody = o.testingAMelody;
		this.validIndexesMap = o.validIndexesMap;
		this.lastIndex = o.lastIndex;
		this.testIndex = o.testIndex;
		this.testInterval = o.testInterval;
		this.testStepInterval = o.testStepInterval;
		this.lastInterval = o.lastInterval;
		this.nextInterval = o.nextInterval;
		this.logginOn = o.logginOn;
		this.nextMotion = o.nextMotion;
		this.testStepIndex = o.testStepIndex;
		this.maxMeasures = o.maxMeasures;
		this.minMeasures = o.minMeasures;
		this.maxNotes = o.maxNotes;
		this.minNotes = o.minNotes;
		this.indexMap = o.indexMap;

		
		validNextIndexes = new NoteIndexCollection();
		for(int i : o.validNextIndexes) {
			this.validNextIndexes.add(i);
		}
		if(null != validNextIndexesSaved) {
			validNextIndexesSaved = new NoteIndexCollection();
			for(int i : o.validNextIndexesSaved) {
				this.validNextIndexesSaved.add(i);
			}			
		}
//		log("validNextIndexes:" + validNextIndexes);
	}
	
	/**
	 * Lots of initial setup to prepare for building a melody. Is this first note 
	 * even valid in the context we are building in? We also try to generate arrays
	 * of valid notes going forward, so as to not waste time exploring notes
	 * we know ahead of time will be invalid. 
	 * 
	 * @param firstNote First note of the melody we are starting to build. 
	 * @return
	 */
	public boolean checkAndSetFirstNote(int firstNote) {
		log("check and set first note:::::::::::::::::" + firstNote);
		testIndex = firstNote;
		if (!rules.checkAgainstParentMelody(noteMelody, firstNote, 0, 0)) {
			log("failed rules check");
			return false;
		}
		if(isCantusFirmus() || isFirstSpecies()) {
			noteMelody.addNote(firstNote, NoteLength.WHOLE_NOTE.noteLength);			
		} else if(isSecondSpecies()) {
			noteMelody.addNote(firstNote, NoteLength.HALF_NOTE.noteLength);						
		}
		setNotesAndRanges();
		int currentStepIndex = indexMap.get(firstNote);
		noteMelody.addStepIndex(currentStepIndex);
		log( "species tyoe: " + rules.speciesType);
		if (isFirstSpecies()) {		//TODO -> need to allow second species start on tritone? Third? 
			log("FIRST SPECIES NOTE ADD?");
			if(!rules.checkLastIndexAsTritone(noteMelody, firstNote, currentStepIndex)) {
				log("invalid tritone start note, exile emlody");
				return false;
				//melodyInProgress.setTritoneResolutionNeeded();
			} else {
				log("return true");
			}
		}
		log("about to generate valid notes arrays");
		generateValidNoteArrays();
		
		validNextIndexes = validIndexesMap.get(noteMelody.size() + 1);
		log("validnextIndexes: " + validNextIndexes);
		
		lastIndex = firstNote;
		return true;
	}
	
	/**
	 * Based on the melody rules, we determine how low and how high the melody can go. A melody that
	 * moves around too much sounds unstable. So the melody is confined from the beginning. Also, we 
	 * determine which indexes to use based on the mode. For example, the second note of an
	 * Ionian scale is a whole step, but the second note of a Locrian scale is a half step. 
	 */
	private void setNotesAndRanges() {
		
		for (int i  = noteMelody.getMinNadirIndex(); i <= noteMelody.getMaxZenithIndex(); i++ ) {
			int y = 0;
			if(i < 0) {
				y = 1;
			}
			int x = (7 + (i%7))%7;
			int f = (noteMelody.getMode().notes[x] + ((i + y)/7)*12 - (y*12));
			indexMap.put(i, f);
		}
	}
	
	private boolean isCantusFirmus() {
		return rules.speciesType.equals(SpeciesType.CANTUS_FIRMUS) ? true : false;
	}
	private boolean isChildSpecies() {
		return rules.speciesType.equals(SpeciesType.CANTUS_FIRMUS) ? false : true;
	}
	private boolean isFirstSpecies() {
		return rules.speciesType.equals(SpeciesType.FIRST_SPECIES) ? true : false;
	}
	private boolean isSecondSpecies() {
		return rules.speciesType.equals(SpeciesType.SECOND_SPECIES) ? true : false;
	}
	private boolean isThirdSpecies() {
		return rules.speciesType.equals(SpeciesType.THIRD_SPECIES) ? true : false;
	}
	private boolean isFourthSpecies() {
		return rules.speciesType.equals(SpeciesType.FOURTH_SPECIES) ? true : false;
	}

	/**
	 * Here we have a partial melody and are testing whether the proposed next note is valid (testIndex)
	 * Thus, we need to check all the rules of our melody, and as soon as our note breaks a rule, we return 
	 * false to indicate to the recursion method that this is an invalid note and to move on to the next one. 
	 * 
	 * The rules are ordered to determine the invalid note as early as possible, for efficiency. 
	 * 
	 * Vernacular is important here: 
	 * 
	 * 	testInterval: Number of half steps from previous note. 
	 * 	pentultimate: Second to last note. Since Fuxian Counterpoint follows strict rules, there 
	 * 					are a lot of constraints around the second to last note, which is the
	 * 					cadence to the conclusion of the melody.
	 *  leap:      	  Jump in the melody that skips notes
	 *  step:		  When the melody moves up or down to the next note in the scale	
	 * 
	 * @param testIndex
	 * @return
	 */
	public boolean testAsNextIndex(int testIndex) {
		
		if(! logCF && isCantusFirmus()) {
			logginOn = false;
		}
		log("valid indexes A : " + validIndexesMap.get(noteMelody.size()+1));
		this.testIndex = testIndex;
		
		testInterval = testIndex - lastIndex;

		if(isFirstSpecies() && noteMelody.size() == noteMelody.getParentMelody().size() - 1) {
			if (!rules.validEndIndexForSpecies(testIndex)) {
				//log("needs to be last note of species but isn't");
				return false;
			}
		}

		/*
		 * If the melody is ready to conclude with a final note, but the current test index is not a
		 * valid final note, then we are no longer ready for the final note, however, processing 
		 * continues because we might still be a valid melody
		 */
		if(noteMelody.finalNoteIsReady()) {
			if ( isCantusFirmus() && testIndex != 0) {
				noteMelody.setFinalNoteNotReady(); 
				noteMelody.setPentultimateFound(false);
				//log("This is not a valid final note for a cantus Firmus!" + melodyInProgress.finalNoteIsReady());
			} 
		}
		
		//is note in bounds of cantus firmus range?
		if(!rules.checkTestNoteRange(testIndex, noteMelody)) {
            //log("Index " + testIndex + " out of range");
			return false;
		}
		
		setTestStepFields();
		
		if(isSecondSpecies()) {
			if(nextNoteDownbeat()) {
				log("verifying next note as a downbeat");
				if(!rules.checkValid2SDownbeat(noteMelody, testIndex, testInterval)) {
					log("not a valid downbeat for 2s given the last halfbeat note: " + testIndex + " testInterval: " + testInterval);
					return false;
				}
			} else {
				log("verifying next note as a halfbeat");
				if(!rules.checkValid2SHalfbeat(noteMelody, testIndex, testInterval)) {
					
					log("not a valid downbeat for 2s given the last halfbeat note: " + testIndex + " testInterval: " + testInterval);
					return false;
				}
			}
		}
		log("valid indexes B : " + validIndexesMap.get(noteMelody.size()+1));
		
		
		if(noteMelody.size() > 0) {
			if(!rules.validMotionCheck(noteMelody, testIndex, testInterval)) {
				return false;
			} 
		}
		
		if(isChildSpecies()) {
			if(!rules.checkAgainstParentMelody(noteMelody, testIndex, testStepIndex, testInterval)) {
				return false;
			}
		}
		
		if(noteMelody.finalNoteIsReady() && !rules.validEndIndexForSpecies(testIndex)) {
			log("invalid index as final note");
			if(isFirstSpecies() && noteMelody.size() == noteMelody.getParentMelody().size() - 1) {
				return false;
			} else if(isSecondSpecies() && noteMelody.melodyLength() == noteMelody.getParentMelody().melodyLength() - 1) {
				log("Needs to be final note for second species" + noteMelody);
				return false;
			}
		}
		log("valid indexes C : " + validIndexesMap.get(noteMelody.size()+1));

		//make sure notes or patterns are not repeating too much within the series. 
		if(!rules.checkNoteRepetition(testIndex, lastIndex, testInterval, noteMelody)) {
			//log("rules note repetition check failed testing: " + testIndex + " for " + melodyInProgress.getAll());
			return false;
		}
		log("Repetition Check Passed");
		
		//Make sure the melody isn't jumping around too much
		if(!rules.checkLeaps(noteMelody, testIndex)) {
			//log("Species Rules Leap Check Fail");
			return false;
		}
		log("Leap Check Passed");
		
		
		
		if(!rules.validTestInterval(noteMelody, testInterval, testStepInterval)) {
			//log("invalid interval test from rules...");
			return false;
		}
		log("Interval Check Passed");
		log("valid indexes D : " + validIndexesMap.get(noteMelody.size()+1));
		
		/*
		 * Two things: if melodyInProgress had pentultimate found, set that, 
		 * if needs to be pentultimate, but isn't, return false. 
		 */
		if(!rules.checkPentultimateFound(noteMelody, testIndex, testInterval)) {
			//log("pentultimate check issue, failing" + testIndex + " for: " + melodyInProgress.getAll());
			return false;
		}
		
		
		nextInterval = testInterval;
		nextMotion = testMotionType;
		return true;
	}

	/**
	 * A valid note has been found so we add it to the melody. Once added, we check whether 
	 * we have completed the melody. 
	 * 
	 * @param interval The number of steps between the prior note and this note.
	 * @return True if we have a completed melody, False if we need to continue building the melody
	 */
	public boolean addIntervalAndCheckForCompletion(int interval) {
		log("!!!!!!adding interval " + interval + "!!!!!");
		log("valid indexes AA: " + validIndexesMap.get(noteMelody.size()+1));
		int noteIndex = lastIndex + interval;
		if(isFirstSpecies() && noteMelody.size() > 0) {
			noteMelody.addMotion(rules.determineMotionType(noteMelody, interval));
		}

		noteMelody.addInterval(interval);
		if(isCantusFirmus() || isFirstSpecies()) {
			noteMelody.addNote(noteIndex, NoteLength.WHOLE_NOTE.noteLength );			
		}
		noteMelody.addStepIndex(indexMap.get(noteIndex));
		
		if(noteMelody.finalNoteIsReady()) {
			if(rules.validEndIndexForSpecies(noteIndex)) {
				return true;
			} 
		}
		
		if(noteMelody.isPentultimateFound()) {
			noteMelody.setFinalNoteReady();
		}
		
		//TODO -- The Tritone code needs to be addressed
		if(isFirstSpecies()) {
			if(!rules.checkLastIndexAsTritone(noteMelody, testIndex, testStepIndex)) {
				//return false;
				//melodyInProgress.setTritoneResolutionNeeded();
			} else {
				//log("tritone resolution form this step isn't needed, set to resolved");
				//melodyInProgress.setTritoneResolved();			
			}	
		}
		
		lastInterval = interval;
		lastIndex = noteIndex;
		pruneValidIndexArrays();
		return false;
	}
	
	public NoteMelodyInProgress getNotes() {
		return noteMelody;
	}

	public List<Integer> getValidNextIndexes() {
		return validNextIndexes.getAll();
	}
	
	public List<Integer>getValidNextIndexesRandomized() {
		return validNextIndexes.getRandomized();
	}
	
	/**
	 * For each note position in the melody, determine a list of potentially valid Note Indexes
	 */
	public void generateValidNoteArrays() {
		validIndexesMap = new HashMap<Integer, NoteIndexCollection>();
		log("generating valid note arrays, are we testing a melody?" + testingAMelody);
		if(testingAMelody) {
			
			if(isCantusFirmus()) {
				generateCFArraysFromTestMelody();				
			} else {
				generateChildArraysFromTestMelody();				
			}
		} else {
			
			switch(rules.speciesType) {
			case FIRST_SPECIES:
				generateS1ArraysFromValidHarmonyIndexes();
				break;
			case SECOND_SPECIES:
				generateS2ArraysFromValidHarmonyIndexes();
				break;
			case CANTUS_FIRMUS:
				generateCFArraysFromValidIndexes();
				break;
			default:
				log("ATTEMPTING TO GENERATE NOTE ARRAYS FOR INVALID SPECIES TYPE!!!");
				break;
			}
		}
	}
	/**
	 * Generate Valid Note Indexes for the Cantus Firmus (Base Species)
	 */
	private void generateCFArraysFromValidIndexes() {
		int c = 0;
		for(int i = 1; i <= maxMeasures; i ++) {
			c++;
			ArrayList<Integer> currIdxList = new ArrayList<Integer>();
			if(i == 1) {
				for(int j : rules.validStartIndexes) {
					currIdxList.add(j);
				}
			} else if(i == maxMeasures) {
				for(int j : rules.validEndIndexes) {
					currIdxList.add(j);
				}
			} else if(i == 2) {
				for(int j : rules.validIntervals) {
					currIdxList.add(j);
				}
			} else {
				for(int j = 0-rules.maxIndexRange(); j<= 0 + rules.maxIndexRange(); j++) {
					currIdxList.add(j);
				}
			}
			if (currIdxList.size() == 0) {
				System.out.println("Trying to have a note with NO valid notes? + i");
				System.exit(0);
			}
			//log("notePos: " + c + "  " + currIdxList);
			validIndexesMap.put(c, new NoteIndexCollection(currIdxList));
		}
	}
	/**
	 * Generate the valid note indexes for a First Species, based onwhich harmonies
	 * are acceptable with the given base melody.
	 */
	private void generateS1ArraysFromValidHarmonyIndexes() {
		int c = 0;
		for (int i : noteMelody.getParentMelody().getAll()) {

			c++;
			ArrayList<Integer> currIdxList = new ArrayList<Integer>();
			
			//If we are getting valid pentultimate array indexes
			if(noteMelody.getParentMelody().size() - 1 == c) {
				log("setting s1 array index pentultimate # " + c + " for cf length: " + noteMelody.parentSize());
				if(noteMelody.getParentMelody().getLastInterval() < 0) {
					//if cantus firmus approaches from below, 1S must approach from above
					for (int k : rules.validEndIndexes) {
						currIdxList.add(k -1);
						if(!noteMelody.getValidPentultimates().contains(k - 1)) {							
							noteMelody.addValidPentultimate(k-1);
						}
					}
				} else {
					for (int k : rules.validEndIndexes) {
						currIdxList.add(k + 1);
						if(!noteMelody.getValidPentultimates().contains(k + 1)) {
							noteMelody.addValidPentultimate(k+1);
						}
					}
				}
			} 
			
			//valid end notes
			if(noteMelody.getParentMelody().size() == c) {
				for(int k : rules.validEndIndexes) {
					currIdxList.add(k);
				}
			}
			
			//for each non pentultimate non whatever...
			if(noteMelody.getParentMelody().size() > c + 1 ) {		
				for(int j : rules.speciesType.validCFHarmonies) {
					if(i + j <= noteMelody.getMaxZenithIndex() &&
					   i+j >= noteMelody.getMinNadirIndex()) {
						currIdxList.add(i + j);
					}
				}
			}
			
			if (currIdxList.size() == 0) {
				log("Trying to have a note with NO valid notes? + i");
				System.exit(0);
			}
			
			//log("notePos: " + c + "  " + currIdxList);
			validIndexesMap.put(c, new NoteIndexCollection(currIdxList));
		}
	}
	/**
	 * Valid Note Indexes for a Second Species
	 */
	private void generateS2ArraysFromValidHarmonyIndexes() {
		int mapIndex = 0;
		mapIndex++;
		validIndexesMap.put(mapIndex, validNextIndexes);
		log("valid NExt INdexes: " + validNextIndexes);
		log("generate s2Arrays from valid Harmony Indexes");
		log("map has: " + validIndexesMap.size() + " entries");
		ArrayList<Integer> currHalfBeatIndexList = new ArrayList<Integer>();
		for(int i : SpeciesType.SECOND_SPECIES.validCFHarmonies) {
			currHalfBeatIndexList.add(i);
		}
				
		
		int c = 0;
		for (int i : noteMelody.getParentMelody().getAll()) {

			c++;
			ArrayList<Integer> currDownBeatIndexList = new ArrayList<Integer>();
			
			//If we are getting valid pentultimate array indexes
			if(noteMelody.getParentMelody().size() - 1 == c) {
				log("setting s1 array index pentultimate # " + c + " for cf length: " + noteMelody.parentSize());
				if(noteMelody.getParentMelody().getLastInterval() < 0) {
					//if cantus firmus approaches from below, 1s must approach from above
					for (int k : rules.validEndIndexes) {
						currDownBeatIndexList.add(k -1);
						if(!noteMelody.getValidPentultimates().contains(k - 1)) {							
							noteMelody.addValidPentultimate(k-1);
						}
					}
				} else {
					for (int k : rules.validEndIndexes) {
						currDownBeatIndexList.add(k + 1);
						if(!noteMelody.getValidPentultimates().contains(k + 1)) {
							noteMelody.addValidPentultimate(k+1);
						}
					}
				}
			} 
			
			//valid end notes
			if(noteMelody.getParentMelody().size() == c) {
				for(int k : rules.validEndIndexes) {
					currDownBeatIndexList.add(k);
				}
			}
			
			//for each non pentultimate non whatever...
			if(noteMelody.getParentMelody().size() > c + 1 ) {		
				for(int j : rules.speciesType.validCFHarmonies) {
					if(i + j <= noteMelody.getMaxZenithIndex() &&
					   i+j >= noteMelody.getMinNadirIndex()) {
						currDownBeatIndexList.add(i + j);
					}
				}
			}
			
			if (currDownBeatIndexList.size() == 0) {
				log("Trying to have a note with NO valid notes? + i");
				System.exit(0);
			}
			
			
			mapIndex++;
			log("for note: " + mapIndex + " currHalfBeatIndex: " + currHalfBeatIndexList); 
			validIndexesMap.put(mapIndex, new NoteIndexCollection(currHalfBeatIndexList));
			mapIndex++;
			log("for note: " + mapIndex + " currDownBeatIndex: " + currHalfBeatIndexList); 
			validIndexesMap.put(mapIndex, new NoteIndexCollection(currDownBeatIndexList));
		}
	}

	/**
	 * When testing a predetermined melody, we limit out valid indexes at each step 
	 * to that exact note. Then if that note fails to be valid, we know which note and why
	 */
	private void generateChildArraysFromTestMelody() {
		int c = 0;
		for(int i : noteMelody.getTestMelody().getAll()) {
			c++;
			List<Integer> currIdxList = new ArrayList<>();
			currIdxList.add(i);
			log("notePos: " + c + "  " + currIdxList);
			validIndexesMap.put(c, new NoteIndexCollection(currIdxList));
		}
	}
	/**
	 * When testing a predetermined melody, set each note as the only valid note 
	 * at that point in the melody. This is what constrains the algorithm to 
	 * following the given Test Melody. 
	 */
	private void generateCFArraysFromTestMelody() {
		int c = 0;

		for(int i : noteMelody.getTestMelody().getAll()) {
			c++;
			List<Integer> currIdxList = new ArrayList<>();
			currIdxList.add(i);
			log("notePos: " + c + "  " + currIdxList);
			validIndexesMap.put(c, new NoteIndexCollection(currIdxList));
		}
	}
	
	/**
	 * Apply some rules here to constrain the next valid notes. For example, if we have to 
	 * resolve a tritone on the next note, we can limit the next valid index to the tritone 
	 * resolution.
	 */
	public void pruneValidIndexArrays() {
		validNextIndexes = new NoteIndexCollection();

		log("valid indexes from map:" + validNextIndexes);
		
		for(int i = noteMelody.size() + 1; i < validIndexesMap.size(); i++) {
			log("valid indexes for step" + i + ":" + validIndexesMap.get(i));
		}
		
		if(noteMelody.pruneIndexArraysForVoiceDisposition()) {
			//log("prune the validIndex arrays for upper?" + melodyInProgress.isUpperVoice() + " lower?" + melodyInProgress.isLowerVoice() );
			pruneForUpperLowerVoice();
			noteMelody.indexArraysPrunedForVoiceDisposition();
		} else {
			for(int i : validIndexesMap.get(noteMelody.size() + 1)) {
				validNextIndexes.add(i);
			}
		}
		log("prune voice disp : " + validIndexesMap.get(noteMelody.size()+1));
		
		if(noteMelody.isTritoneResolutionNeeded()) {
			log("Need log to prune everything that isn't a resolution of the next tritone");
			NoteIndexCollection tritoneResolutionIndex = new NoteIndexCollection();
			
			if(noteMelody.getParentMelody().getInterval(noteMelody.size()) == 1) {
				for(int j : validNextIndexes) {
					if(j - noteMelody.getLast() == -1) {
						tritoneResolutionIndex.add(j);
					}
				}
				//validNextIndexes.add(-1);
			} else if(noteMelody.getParentMelody().getInterval(noteMelody.size()) == -1) {
				//validNextIndexes.add(1);
				for(int j : validNextIndexes) {
					if(j - noteMelody.getLast() == 1) {
						tritoneResolutionIndex.add(j);
					}
				}
				
			}
			validNextIndexes = tritoneResolutionIndex;
			
		} else if(noteMelody.leapTally() >= rules.maxLeaps) {
			NoteIndexCollection leapRestraintIndexes = new NoteIndexCollection();
			for(int j : validNextIndexes) {
				if(Math.abs(noteMelody.getLast() - j) == 1) {
					leapRestraintIndexes.add(j);
				}
			}
			validNextIndexes = leapRestraintIndexes;
			log("leap prune : " + validIndexesMap.get(noteMelody.size()+1));
		}

		log("final form validNextIndexes:" + validNextIndexes);
			
	}
	/**
	 * Voices should not cross so if the countermelody is above the Base Melody, we know all 
	 * subsequent notes for this melody must be above the Base Melody at that given note.
	 */
	private void pruneForUpperLowerVoice() {
		if(isFirstSpecies()) {
			int nextIdx = noteMelody.size() + 1;
			NoteIndexCollection toFilter = validIndexesMap.get(nextIdx);
			for(int i : toFilter) {
				if(noteMelody.isUpperVoice() &&
						noteMelody.getParentMelody().get(nextIdx) < i) {
					validNextIndexes.add(i);
				} else if(noteMelody.isLowerVoice() && 
						noteMelody.getParentMelody().get(nextIdx) > i) {
					validNextIndexes.add(i);
				}
			}
		} else {
			log("pruning for something unhandled:" + rules.speciesType.name);
		}
		
	}
	
	/**
	 * Only allow consonant harmonies between index1 and index2
	 * @param index1
	 * @param index2
	 * @param indexList
	 */
	public void addIfConsonantHarmony(int index1, int index2, NoteIndexCollection indexList) {
		log("have we determine upper vs lower voice? upper: " + noteMelody.isUpperVoice() + "lower:" + noteMelody.isLowerVoice());
		if(rules.isConsonantHarmony(index1, index2)) {
			log("Consonant; add; pass:" + index1 + " vs: " + index2);
			indexList.addIfNotDuplicate(index1);
		} else {
			log("Not consonant, pass:" + index1 + " vs: " + index2);
		}
	}
	
	/**
	 * Determine if the next note is a downbeat
	 * @return
	 */
	private boolean nextNoteDownbeat() {
		if(isSecondSpecies()) {
			//this is a stupid way to determine this, I'm sure there's a better way. 
			log("noteMelody.melodyLength()" + noteMelody.melodyLength() );
			log("note melody length%1: " + noteMelody.melodyLength()%1 );
			if((noteMelody.melodyLength()) % 1 == 0) {
				return true;
			} else {
				return false;
			}
		} else {
			log("lastNoteDownbeat called for unhandled species type:" + rules.speciesType);
		}
		return false;
	}

	public List<Integer> getNextValidIndexArray() {
		return validIndexesMap.get(noteMelody.size() + 1).getAll();
	}

	public List<Integer> getNextValidIndexArrayRandomized() {
		if(null == noteMelody) {
			log("notes are null");
		}
		return validNextIndexes.getRandomized();
	}
	
	public SpeciesSystem getSpeciesSystem() {
		return rules.speciesType.speciesSystem;
	}
	
	public void log(String msg) {
		if(logginOn) {
			System.out.println("SpeciesBuilder-Log:   " + msg + "   testIndex " + testIndex + "         melody: " + noteMelody.getAll() );
		}
	}

	public NoteMelodyInProgress getMelody() {
		return noteMelody;
	}
	
	private void setTestStepFields() {
		testStepIndex = indexMap.get(testIndex);
		testStepInterval = testStepIndex - indexMap.get(lastIndex);
	}

}
