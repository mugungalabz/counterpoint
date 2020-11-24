package com.mugunga.counterpoint;

import com.mugunga.musicmodels.NoteLength;

public class TestMelody extends NoteMelody {

	public TestMelody() {
		
	}
	
	public TestMelody(int[] notes, NoteLength uniformLength) {
		setUniformLengthMelody(notes, uniformLength);
	}

	public TestMelody(int[] test2sMelody, double[] test2sLengths) {
		setNotes(test2sMelody, test2sLengths);
	}
}
