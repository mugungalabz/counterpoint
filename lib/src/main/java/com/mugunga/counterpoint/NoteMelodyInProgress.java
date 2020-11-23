package com.mugunga.counterpoint;
import java.util.ArrayList;

import com.mugunga.musicmodels.Mode;
import com.mugunga.musicmodels.Note;

/**
 *  A Note Melody that is a type of Note Melody, but it tracks certain characteristics that need to be met for
 *  the melody to be complete, mostly in the form of boolean switchs that are turned on when certain conditions are 
 *  met for this melody
 */
public class NoteMelodyInProgress extends NoteMelody {
	
	private boolean tritoneResolutionNeeded;
	private boolean triggerValidIndexArrayPruning;
	private boolean pruneIndexArraysForVoiceDisposition;
	private boolean pentultimateFound;
	private boolean finalNoteReady;
	private boolean skipFirstDownbeat;
	private boolean pentultimateWholeNote;
	
	//valid notes for the second to last note, do we need to add 5-1 to allow fifths? 
	private ArrayList<Integer> validPentultimates = new ArrayList<Integer>();
	
	public NoteMelodyInProgress() {
		
	}
	
	public NoteMelodyInProgress(NoteMelodyInProgress melody) {
		super(melody);
		this.tritoneResolutionNeeded = melody.tritoneResolutionNeeded;
		this.triggerValidIndexArrayPruning = melody.triggerValidIndexArrayPruning;
		this.pruneIndexArraysForVoiceDisposition = melody.pruneIndexArraysForVoiceDisposition;
		this.pentultimateFound = melody.pentultimateFound;
		
		this.finalNoteReady = melody.finalNoteReady;
		this.skipFirstDownbeat = melody.skipFirstDownbeat;
		this.pentultimateWholeNote = melody.pentultimateWholeNote;
//		this.killMelody = melody.killMelody;
//		log("melody clone constructor:" + finalNoteReady);
//		log("melody clone constructor:" + pentultimateFound);
		for(int i : melody.validPentultimates) {
			validPentultimates.add(i);
		}
		
	}
	
	public NoteMelodyInProgress(SpeciesRules rules, Mode mode) {
		super(rules, mode);
		rules.setMelodyPentultimates(this);
	}
	

	public void setTritoneResolutionNeeded() {
		tritoneResolutionNeeded = true;
	}
	
	public void setTritoneResolved() {
		if(tritoneResolutionNeeded) {
			log("RESOLVING TRITTTTONE!" + getAll());
			log("w/ parent: " + parentNoteMelody.getAll());
			
		}
		tritoneResolutionNeeded = false;
	}
	
	public boolean isTritoneResolutionNeeded() {
		return tritoneResolutionNeeded;
	}
	
	private void log(String msg) {
		System.out.println("MelodyInProgLog:      " + msg + "     maxNote: " + rules.maxMeasures);
	}
	
	public void addNote(int newNote, double length) {
		super.addNote(newNote, length);
		
		if(!upperVoice && !lowerVoice && hasParentMelody()) {
			int cfNote = parentNoteMelody.getFirst();
			if(newNote > cfNote) {
//				log("setting upper voice to true in MIP:" + newNote + " vs cf note: " + cfNote);
				upperVoice = true;
				pruneIndexArraysForVoiceDisposition = true;
			} else if(newNote< cfNote) {
//				log("setting lower voice to true in MIP:" + newNote + " vs cf note: " + cfNote);
				lowerVoice = true;
				pruneIndexArraysForVoiceDisposition = true;
			}
		}
	}

	public boolean pruneIndexArraysForVoiceDisposition() {
		return pruneIndexArraysForVoiceDisposition;
	}

	public void indexArraysPrunedForVoiceDisposition() {
//		log("index arrays have been pruned!!");
		pruneIndexArraysForVoiceDisposition = false;
	}
	
	public boolean isPentultimateFound() {
		return pentultimateFound;
	}
	
	public void setPentultimateFound(boolean pentultimateFound) {
//		log("actually seting P found");
		this.pentultimateFound = pentultimateFound;
	}

	public void addValidPentultimate(int noteIndex) {
		validPentultimates.add(noteIndex);
	}

	public ArrayList<Integer> getValidPentultimates() {
		
		return validPentultimates;
	}

	public void setFinalNoteReady() {
		finalNoteReady = true;
	}
	
	public void setFinalNoteNotReady() {
		finalNoteReady = false;
	}

	public boolean finalNoteIsReady() {
		return finalNoteReady;
	}

	public int getParentNote(int i) {
		return parentNoteMelody.get(i);
	}

	public int parentSize() {
		return parentNoteMelody.size();
	}

	/**
	 * Note, this gets the previous parent note in relation to where the working melody is at within the melody. 
	 * @return
	 */
	public int getPreviousParentNote() {
		double melodyLength = melodyLength();
		double parentLength = 0;
		int savedNoteIndex = 0;
		for(Note note : parentNoteMelody) {
			if(parentLength + note.noteLength() > melodyLength) {
				return savedNoteIndex;
			} else {
				savedNoteIndex = note.index();
				parentLength += note.noteLength();
			}
		}
		return 0;
	}

	public int getNextParentNote() {
		double melodyLength = melodyLength();
		double parentLength = 0;
		for(Note note : parentNoteMelody) {
			if(parentLength + note.noteLength() > melodyLength) {
				return note.index();
			} else {
				parentLength += note.noteLength();
			}
		}
		return 0;
	}







}
