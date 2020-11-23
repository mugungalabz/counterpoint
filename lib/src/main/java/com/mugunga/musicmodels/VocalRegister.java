package com.mugunga.musicmodels;


public enum VocalRegister {
	//MIDDLE C is C4
	SOPRANO ("C4", "C6"),
	MEZZO ("A3", "A5"),
	CONTRALTO ("F3", "E5"),
	TENOR ("B2", "A4"),
	BARITONE ("G2", "F4"),
	BASS ("E2", "E4");
	
	private final String lowRange;
	private final String highRange;
	VocalRegister(String low, String high) {
		this.lowRange= low;
		this.highRange= high;
	}
	
	

	
	
	
//	Soprano: the highest female voice, being able to sing C4 (middle C) to C6 (high C), and possibly higher.
//	Mezzo-soprano: a female voice between A3 (A below middle C) and A5 (two octaves above A3).
//	Contralto: the lowest female voice, F3 (F below middle C) to E5. Rare contraltos possess a range similar to the tenor.
//	Tenor: the highest male voice, B2 (2nd B below middle C) to A4 (A above Middle C), and possibly higher.
//	Baritone: a male voice, G2 (two Gs below middle C) to F4 (F above middle C).
//	Bass: the lowest male voice, E2 (two Es below middle C) to E4 (the E above middle C).
}
