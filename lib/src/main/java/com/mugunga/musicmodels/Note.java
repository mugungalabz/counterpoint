package com.mugunga.musicmodels;

import com.mugunga.musicmodels.NoteLength;

public class Note {
	
	/*
	 * The keyIndex is a numerical representation of the index within a normal 7 note scale. 
	 * Thus, it would go as follows:  
	 * 
	 * 0 = Tonic
	 * 1 = second
	 * 2 = 3rd
	 * 3 = fourth
	 * 4 = fifth
	 * 5 = sixth
	 * 6 = seventh
	 * 7 = octave
	 * 8 = ninth
	 * etc. 
	 * 
	 * Thus, accidentals and sharps are not accounted for here, and become implied when 
	 * applying certain rules. 
	 */
	private int keyIndex;
	private double noteLength;
	
	public Note(int key, double length) {
		this.keyIndex = key;
		this.noteLength = length;
	}
	
	public Note(int index, NoteLength noteLength) {
		this.keyIndex = index;
		this.noteLength = noteLength.noteLength;
	}
	
	public int index() {
		return keyIndex;
	}
	
	public double noteLength() {
		return noteLength;
	}
}
