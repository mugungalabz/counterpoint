package com.mugunga.counterpoint;
/**
 * A Species System defines the entire paradigm of melodic structure. For example, Fuxian Counterpoint is the 
 * set of rules set forth by composer Johann Joseph Fux. This can be thought of as global constraints for a given
 * collection of Species Types while constructing melodies. 
 *  
 * @author laurencemarrin
 *
 */
public enum SpeciesSystem {
	
	FUXIAN_COUNTERPOINT ( 9, //minimum # of measures in a parent melody 
						  13,  //maximum # of measures in a parent melody //TODO rule for how many notes in measure
						  2,  //minimum # of leaps/species
						  4, //maximum # of leaps/species
						  4, //maximum # of occurences per a note
						  9, //maximum species range
						  4, //minimum species range
						  new int[] {0, 2, 4, 5, 7}, //consonant intervals
						  true),
	
	MARRIN_COUNTERPOINT ( 9, //minimum # of notes in a parent melody 
			13,  //maximum # of notes in a parent melody
			2,  //minimum # of leaps/species
			4, //maximum # of leaps/species
			4, //maximum # of occurences per a note
			9, //max species index range
			4, //minimum species index range
			new int[] {}, //consonant intervals
			true),
	
	TEMPLATE_FOR_COUNTERPOINT_SYSTEM ( 9, 
			13,  
			2,  
			4, 
			4, 
			9, //max species index range
			4, //min species index range
			new int[] {}, //consonentIntervals
			false);
	
	final public int[] consonantIntervals;
	final public int minMeasures;
	final public int maxMeasures;
	final public int minLeaps;
	final public int maxLeaps;
	final public int maxNoteOccurrences;
	final public int maxIndexRange;
	final public int minIndexRange;
	
	SpeciesSystem (int minMeasures,
				   int maxMeasures,
				   int minLeaps,
				   int maxLeaps,
				   int maxNoteOccurrences,
				   int maxRange,
				   int minRange,
				   int[] consonantIntervals,
				   boolean nothing) {
		
		this.minMeasures = minMeasures;
		this.maxMeasures = maxMeasures;
		this.minLeaps = minLeaps;
		this.maxLeaps = maxLeaps;
		this.maxIndexRange = maxRange;
		this.minIndexRange = minRange;
		this.maxNoteOccurrences = maxNoteOccurrences;
		this.consonantIntervals = consonantIntervals;
	}

}
