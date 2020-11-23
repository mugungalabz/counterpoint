package com.mugunga.musicmodels;

public enum NoteLength {
	
	WHOLE_NOTE(1),
	HALF_NOTE(.5),
	QUARTER_NOTE(.25);
	
	public final double noteLength;
	
	NoteLength(double noteLength) {
		this.noteLength = noteLength;
	}

}
