package com.mugunga.counterpoint;
import java.util.ArrayList;
import java.util.List;

import com.mugunga.musicmodels.IntervalIndexCollection;
import com.mugunga.musicmodels.Mode;
import com.mugunga.musicmodels.Note;
import com.mugunga.musicmodels.NoteCollection;
import com.mugunga.musicmodels.StepIndexCollection;

public abstract class NoteMelody extends NoteCollection {
	
	protected SpeciesRules rules;
	
	private boolean uniqueZenith = false;
	private boolean uniqueNadir = false;
	
	private int zenithPos;
	private int nadirPos;
	private int climaxPos;
	public boolean validZenith;
	public boolean validNadir;
	public boolean raisedLeadingTone;
	private int minNadirIndex;
	private int maxZenithIndex;
	private List<Integer> validZenithIndexes = new ArrayList<>();
	private List<Integer> validNadirIndexes = new ArrayList<>();
	private StepIndexCollection stepIndexes = new StepIndexCollection();
	private List<Integer> motions = new ArrayList<>();
	private List<Integer> downBeats = new ArrayList<>();
	protected NoteMelody parentNoteMelody;
	private TestMelody testMelody;
	protected TestMelody testChildMelody;
	private int noteRepeats;
	private Mode mode;
	
	public boolean upperVoice = false;
	public boolean lowerVoice = false;
	
	private IntervalIndexCollection intervals = new IntervalIndexCollection();
	
	public NoteMelody() {
		
	}

	/**
	 * 
	 * @param rules
	 * @param mode
	 * 
	 * Use this constructor for an intial melody which only knows its particular rules (ex fuxian counterpoint)
	 * and mode (ex Dorian). It is not subject to any other melodies. 
	 */
	public NoteMelody(SpeciesRules rules, Mode mode) {
		this.rules = rules;
		this.mode = mode;
//		log("Melody rulesOnly constructor rules object: " + rules);
	}
	
	/**
	 * 
	 * @param parent
	 * 
	 * Use this constructor when the melody is building upon a particular parent melody and must 
	 * follow the predetermined rules. For example: in Fuxian counterpoint, the parent melody would be the Cantus Firmus
	 * which means a First Species or Second Species melody needs to subject itself to it in this constructor. Thus,
	 * the parent melody needs to be fed in and obviously contains all relevant information. 
	 */
	public NoteMelody (NoteMelody parent) {
		addAllNotes(parent);
		this.rules = parent.rules;
		this.mode = parent.mode;
		this.parentNoteMelody = parent.parentNoteMelody;
		this.uniqueNadir = parent.uniqueNadir;
		this.uniqueZenith = parent.uniqueZenith;
		this.validZenith = parent.validZenith;
		this.validNadir = parent.validNadir;
		this.raisedLeadingTone = parent.raisedLeadingTone;
		this.climaxPos = parent.climaxPos;
		this.zenithPos = parent.zenithPos;
		this.nadirPos = parent.nadirPos;
		this.minNadirIndex = parent.minNadirIndex;
		this.maxZenithIndex = parent.maxZenithIndex;
		this.noteRepeats = parent.noteRepeats;
		this.upperVoice = parent.upperVoice;
		this.lowerVoice = parent.lowerVoice;
		
		for(int i : rules.validNadirIndexesPrimitive) {
			this.validNadirIndexes.add(i);
		}
		
		//this.validZenithIndexes = new ArrayList<Integer>();
		for(int i : rules.validZenithIndexesPrimitive) {
			this.validZenithIndexes.add(i);
		}
		
		this.intervals = new IntervalIndexCollection();
		for (int i : parent.getIntervals().getAll()) {
			this.intervals.add(i);
		}
		
		for (int i : parent.stepIndexes.getAll()) {
			this.stepIndexes.add(i);
		}
		
		for(int i : parent.motions) {
			this.motions.add(i);
		}
		
		for(int i : parent.downBeats) {
			this.downBeats.add(i);
		}
	}

	//this method is used instead of add to perform extra maintenance logic every time a note is added. 
	public void addNote(int newNote, double length) {
		super.add(new Note(newNote, length));
		checkInternals(newNote);
	}
	
	public void addStepIndex(Integer newStepIndex) {
		stepIndexes.add(newStepIndex);
		
	}
	
	private void checkInternals(int newNote) {
		if(size() > 1) {
			
			
			if(newNote == get(-2)) {
				noteRepeats++;
			}
			
			if(newNote > zenith()) {
				//log("New note greater than current Zenith");
				zenithPos = notes.size();
				uniqueZenith = true;
				minNadirIndex = (minNadirIndex < (zenith() - rules.maxIndexRange())) ? (zenith() - rules.maxIndexRange()) : minNadirIndex;
				//log("new zenith: " + zenith() + " minNadirIndex: " + minNadirIndex + " rules.maxIndexRage: " + rules.maxIndexRange);
			} else if (newNote == zenith()) {
				uniqueZenith = false;
			}
			if(newNote < nadir()) {
				nadirPos = notes.size();
				uniqueNadir = true;
				maxZenithIndex = (maxZenithIndex > (nadir() + rules.maxIndexRange())) ? (nadir() + rules.maxIndexRange()) : maxZenithIndex;
				//log("new nadir: " + nadir() + " maxZenithIndex: " + maxZenithIndex + " rules.maxIndexRage: " + rules.maxIndexRange);
			} else if (newNote == nadir()) {
				uniqueNadir = false;
			}

		} else if(size() == 1) {
			minNadirIndex = getFirst() - rules.maxIndexRange();
			maxZenithIndex = getFirst() + rules.maxIndexRange();
//			log("minNadirIndex" + minNadirIndex);
//			log("maxZenithIndex" + maxZenithIndex);
			
			for(int i : rules.validZenithIndexesPrimitive) {
				this.validZenithIndexes.add(i + getFirst());
			}
			for(int i : rules.validNadirIndexesPrimitive) {
				this.validNadirIndexes.add(i + getFirst());
			}
		}
	}
	
	protected boolean hasParentMelody() {
		//log("Checking parent Melody for this " + rules.speciesType + ": " + parentMelody );
		return null == parentNoteMelody ? false : true;
	}

	public int zenith() {
		return get(zenithPos);
	}
	
	public int nadir() {
		return get(nadirPos);
	}
	
	public int zenithPos() {
		return zenithPos;
	}
	
	public int nadirPos() {
		return nadirPos;
	}
	
	public boolean uniqueZenith() {
		return uniqueZenith;
	}
	public boolean uniqueNadir() {
		return uniqueNadir;
	}

	public int getLastInterval() {
//		log("getLast()" + getLast() + "; get(-1)" + get(-1));
		return (getLast() - get(-2));
	}

	public int getInterval(int intervalIndex) {
		return intervals.get(intervalIndex);
		//return get(intervalIndex) - get(intervalIndex - 1);
	}
	
	public int getMinNadirIndex() {
		return minNadirIndex;
	}
	
	public int getMaxZenithIndex() {
		return maxZenithIndex;
	}
	
	public boolean checkIfValidZenithIndex(int testIndex) {
		if(this.validZenithIndexes.contains((get(1) + testIndex)%7)) {
			return true;
		}
		return false;
	}
	
	public boolean checkIfValidNadirIndex(int testIndex) {
//		log("validNadirIndexs" + validNadirIndexes.toString() + "testIndex:" + testIndex );
		if(this.validNadirIndexes.contains((get(1) + testIndex)%7)) {
			return true;
		}
		return false;
	}
	
	public void setParentMelody(NoteMelody parentMelody) {
		this.parentNoteMelody = parentMelody;
//		ArrayList<Integer> parentNoteIndexes = new ArrayList<Integer>();
//		for(Note note : parentMelody) {
//			parentNoteIndexes.add(note.index());
//		}
//		this.parentMelody = parentNoteIndexes;
	}

	
	public NoteMelody getParentMelody() {
		return parentNoteMelody;
	}
	private void log(String msg) {
		System.out.println("Melody-Log:           " + msg);
	}

	public int zenithMagnitude() {
		return Math.abs(zenith() - getFirst());
	}
	public int nadirMagnitude() {
		return Math.abs(getFirst() - nadir());
	}
	
	public void annealZenith() {
//		log("annealing Zenith" + zenithPos);
		validZenith = true;
		validNadir = false;
		climaxPos = zenithPos;
	}
	
	public void annealNadir() {
//		log("annealing Nadir" + nadirPos);
		validZenith = false;
		validNadir = true;	
		climaxPos = nadirPos;
	}
	
	int modeNotesTally(int noteIndex) {
		int t = 0;
		int testNote = noteIndex %7;
		for (int i: getAll()) {
			if((i + 14)%7 == testNote) {
				t++;
			}
		}
		//log("returning mode tally of: " + t);
		return t;
	}
	
	int downbeatNotesTally(int noteIndex) {
		
		int t = 0;
		int testNote = noteIndex %7;
		
		if(rules.speciesType == SpeciesType.SECOND_SPECIES) {
			for(int i = 0; i <= size(); i += 2) {
				if((i + 14)%7 == testNote) {
					t++;
				}
			}
		} else {
			t = modeNotesTally(noteIndex); //TODO verify for cantus firmus. 
		}
		
		return t;
	}

	public int getClimaxPos() {
		return climaxPos;
	}

	public IntervalIndexCollection getIntervals() {
		return intervals;
	}
	
	public int leapTally() {
		return intervals.leapTally();
	}

	public void addInterval(int interval) {
		intervals.add(interval);
		
	}

	public int noteRepeatCount() {
		return noteRepeats;
	}
	
	public void incrementNoteRepeat() {
		noteRepeats++;
		
	}
	
	public SpeciesType getSpeciesType() {
		return rules.speciesType;
	}
	
	public int getStepIndex(int notePos) {
		
		return stepIndexes.get(notePos);
	}
	
	public StepIndexCollection getStepIndexes() {
		return stepIndexes;
	}
	
	public String getNotesAsCSV() {
		return getAll().toString().replaceAll("[\\[\\]]", "");
	}
	
	public Mode getMode() {
		return mode;
	}
	
	public void tailorStepIndexes() {
		stepIndexes = new StepIndexCollection(rules.tailorStepIndexes(this));
	}

	public boolean isUpperVoice() {
		return upperVoice;
	}
	
	public boolean isLowerVoice() {
		return lowerVoice;
	}

	public int getLastStepIndex() {
		return stepIndexes.getLast();
	}

	public void setRaisedLeadingTone(boolean isLeadingToneRaised) {
		raisedLeadingTone = isLeadingToneRaised;
	}
	
	public boolean isLeadingToneRaised() {
		return raisedLeadingTone;
	}
	
	public List<Integer> getMotions() {
		return motions;
	}
	
	public void addMotion(int newMotion) {
		motions.add(newMotion);
	}
	
	public void setChildSpeciesTest(TestMelody testMelody) {
		this.testChildMelody = testMelody;
	}
	
	public TestMelody getChildSpeciesTestMelody() {
		return this.testChildMelody;
	}
	
	public TestMelody getTestMelody() {
		return testMelody;
	}
	
	public boolean isTestingChildSpecies() {
		return null == testChildMelody ? false : true;
	}
	
	public double melodyLength() {
		double melodyLength= 0;
		for(Note note : notes ) {
			melodyLength += note.noteLength();
		}
		return melodyLength;
	}
	
	public double parentMelodyLength() {
		
		return parentNoteMelody.melodyLength();
	}
	
	public void setTestMelody(TestMelody testMelody) {
		this.testMelody = testMelody;
		
	}
	
	public double testMelodyLength() {
		return testMelody.melodyLength();
	}
	
	public int getModeID() {
		return mode.modeID;
	}
	
	public String getStepIndexesAsCSV() {
		return stepIndexes.toString().replaceAll("[\\[\\]]", "");
	}
	
}
