package com.mugunga.musicmodels;


public class TimeSignature {
	private final int upperNumeral;
	private final int lowerNumeral;
	
	public TimeSignature(int upper, int lower) {
		upperNumeral = upper;
		lowerNumeral = lower;
	}
	
	public String display() {
		return upperNumeral + "/" + lowerNumeral;
	}
}
