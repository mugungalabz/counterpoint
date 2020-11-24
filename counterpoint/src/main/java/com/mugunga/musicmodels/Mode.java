package com.mugunga.musicmodels;
/**
 * A Mode represents a musical scale. 
 * @author laurencemarrin
 *
 */
public enum Mode {
	IONIAN(new int[] {0, 2, 4, 5, 7, 9, 11, 12}, "IONIAN",1), //-> tested
	DORIAN(new int[] {0, 2, 3, 5, 7, 9, 10, 12},"DORIAN",2 ),
	PHYRGIAN(new int[] {0, 1, 3, 5, 7, 8, 10, 12},"PHRYGIAN",3 ),
	LYDIAN(new int[] {0, 2, 4, 6, 7, 9, 11, 12 },"LYDIAN",4),
	MIXOLYDIAN(new int[] {0, 2, 4, 5, 7, 9, 10, 12 },"MIXOLYDIAN",5),
	AEOLIAN(new int[] {0, 2, 3, 5, 7, 8, 10, 12 },"AEOLIAN",6),
	LOCRIAN(new int[] {0, 1, 3, 5, 6, 8, 10, 12 },"LOCRIAN",7);
	
	
	final public int[] notes;
	final public String modeName;
	final public int modeID;
	/**
	 * Enum constructor
	 * @param notes
	 * @param modeName
	 * @param modeID
	 */
	Mode(int[] notes, String modeName, int modeID) {
		this.notes = notes;
		this.modeName = modeName;
		this.modeID = modeID;
	}

}
