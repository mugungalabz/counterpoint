package com.mugunga.musicmodels;
/**
 * In music theory, an interval is the number of steps between two notes. These intervals are used to 
 * describe music, most importantly, to stack on top of each other to construct chords. 
 * 
 * @author laurencemarrin
 *
 */
public enum Interval {
	UNISON(0, 0),
	MINOR_2ND(1, 1),
	MAJOR_2ND(2, 1),
	MINOR_3RD(3, 2),
	MAJOR_3RD(4, 2),
	PERFECT_4TH(5, 3),
	TRITONE(6, 4),
	PERFECT_5TH(7, 4),
	AUGMENTED_5TH(8, 4),
	MINOR_SIX(8, 5),
	MAJOR_SIX(9, 5),
	MINOR_7TH(10, 6),
	MAJOR_7TH(11, 6),
	OCTAVE(12,7),
	NOTHING(911, 911);
	
	
	public final int steps;
	public final int modeIndex;
	/**
	 * Enum constructor
	 * @param steps
	 * @param modeIndex
	 */
	Interval(int steps, int modeIndex) {
		this.steps = steps;
		this.modeIndex = modeIndex;
	}
	/**
	 * Given a number of steps, return the type of interval those steps spanned
	 * @param steps
	 * @return
	 */
	public static Interval getInterval(int steps) {
		for(Interval i : Interval.values()) {
			if(steps == i.steps) {
				return i;
			}
		}
		log("INVALID INTERVAL REQUESTED");
		return NOTHING;
	}
	
	private static void log(String msg) {
		System.out.println("Interval Log     :" + msg);
	}
	
}
