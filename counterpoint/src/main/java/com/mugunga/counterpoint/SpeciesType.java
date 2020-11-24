package com.mugunga.counterpoint;

/**
 * Species Type is a particular set of rules that applies to a melody within a certain System of music. For example, 
 * in Fuxian counterpoint, the Cantus Firmus is the base or starting melody, and the first and second species are
 * melodies built upon the Cantus Firmus that follow certain rules. 
 * 
 * Similarly, when writing a an orchestral piece, each melody of the other instruments will have constraints based on 
 * their natural ranges, and in relation to the primary theme of the piece. 
 * 
 * A full song would generally consist of multiple species types. 
 * 
 * 
 * @author laurencemarrin
 *
 */
public enum SpeciesType {
	
	CANTUS_FIRMUS("Cantus Firmus",
			SpeciesSystem.FUXIAN_COUNTERPOINT,
			new int[] {-2, 1, 2, 4, -1,-3, -4, 5, 3, 7, -7}, //valid intervals from note to note
			new int[] {}, //valid harmonies with cantus firmus (NA for actual cantus firmus
			new int[] {}, //valid step harmonies with cantus firmus (NA for actual cantus firmus
			new int[] {0}, //valid start indexes
			new int[] {0}, //valid end indexes
			new int[] {6, 10, 11}, //invalid Step Outlines
			0,                     //maximum # of note repeats
			true),
	
	FIRST_SPECIES("First Species",
			SpeciesSystem.FUXIAN_COUNTERPOINT,
			new int[] {1, -1, 2, -2, 3, -3, 4, -4, 5, 6, -6}, //valid intervals from note to note
			new int[] {-11, -9, -7, -5, -4, -2, 0, 2, 4, 5, 7, 9, 11}, //valid harmonies w/cantus firmus
			//TODO get rid of bounds?
			new int[] {3, 4, 6, 7, 8, 9, 12, 15, 16, 18, 19, 0, -3, -4, -6, -7, -8, -9, -12, -15, -16, -18, -19}, //valid Step INdex harmonies w/ cantus Firmus
			new int[] {7, 4, 0}, //valid start indexes
//			new int[] {4}, //valid start indexes
			new int[] {0, 7}, //valid end indexes
			new int[] {6, 10, 11}, //invalid Step Outlines
			1, 						//maximum # of note repeats
			false),
	
	SECOND_SPECIES("Second Species",
			SpeciesSystem.FUXIAN_COUNTERPOINT,
			new int[] {1, -1, 2, -2, 3, -3, 4, -4, 5, 6, -6, 7, -7},     //valid intervals between notes
			new int[] {-11, -9, -7, -5, -4, -3, -2, -1,  0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},   // valid index harmonies w/ cantus firmus
			new int[] {3, 4, 6, 7, 8, 9, 12, 15, 16, 18, 19, 0, -3, -4, -6, -7, -8, -9, -12, -15, -16, -18, -19},   // valid step index harmonies w/ cantus firmus
			new int[] {7, 4, 2, 0, },//valid start indexes
			new int[] {0, 7},//valid end indexes
			new int[] {6, 10, 11}, //invalid Step Outlines
			5,                     //maximum # of note repeats 
			false),
	THIRD_SPECIES("Third Species",
			SpeciesSystem.FUXIAN_COUNTERPOINT,
			new int[] {-2, 1, 2, 4, -1,-3, -4, 5, 3, 7, -7},     //valid intervals between notes
			new int[] {-11, -9, -7, -5, -4, -2, 0, 2, 4, 5, 7, 9, 11},   // valid index harmonies w/ cantus firmus
			new int[] {3, 4, 6, 7, 8, 9, 12, 15, 16, 18, 19, 0, -3, -4, -6, -7, -8, -9, -12, -15, -16, -18, -19},   // valid step index harmonies w/ cantus firmus
			new int[] {},//valid start indexes
			new int[] {0, 7},//valid end indexes
			new int[] {6, 10, 11}, //invalid Step Outlines
			5,                     //maximum # of note repeats 
			false),
	FOURTH_SPECIES("Fourth Species",
			SpeciesSystem.FUXIAN_COUNTERPOINT,
			new int[] {-2, 1, 2, 4, -1,-3, -4, 5, 3, 7, -7},     //valid intervals between notes
			new int[] {-11, -9, -7, -5, -4, -2, 0, 2, 4, 5, 7, 9, 11},   // valid index harmonies w/ cantus firmus
			new int[] {3, 4, 6, 7, 8, 9, 12, 15, 16, 18, 19, 0, -3, -4, -6, -7, -8, -9, -12, -15, -16, -18, -19},   // valid step index harmonies w/ cantus firmus
			new int[] {},//valid start indexes
			new int[] {0, 7},//valid end indexes
			new int[] {6, 10, 11}, //invalid Step Outlines
			5,                     //maximum # of note repeats 
			false),
	MUGUNGRAL_BASE_CHORD("Mugungral Base Chords",
			SpeciesSystem.MARRIN_COUNTERPOINT,
			new int[] {},     //valid intervals between notes
			new int[] {},   // valid index harmonies w/ cantus firmus
			new int[] {},   // valid step index harmonies w/ cantus firmus
			new int[] {},//valid start indexes
			new int[] {},//valid end indexes
			new int[] {6, 10, 11}, //invalid Step Outlines
			5,                     //maximum # of note repeats 
			false),
	
	SPECIES_TYPE_TEMPLATE("Template for new species types",
			SpeciesSystem.MARRIN_COUNTERPOINT,
			new int[] {},     //valid intervals between notes
			new int[] {},   // valid index harmonies w/ cantus firmus
			new int[] {},   // valid step index harmonies w/ cantus firmus
			new int[] {},//valid start indexes
			new int[] {},//valid end indexes
			new int[] {6, 10, 11}, //invalid Step Outlines
			5,                     //maximum # of note repeats 
			false);
	

	
	final public String name;
	final public SpeciesSystem speciesSystem;
	final public int[] validIntervals;
	final public int[] validCFHarmonies;
	final public int[] validCFStepHarmonies;
	final public int[] validStartIndexes;
	final public int[] validEndIndexes;
	final public int[] invalidStepOutlines;
	final public int maxNoteRepeats;
	final public boolean parentSpecies;
	
	
	SpeciesType(String name, 
			    SpeciesSystem speciesSystem,
				int[] validIntervals,
				int[] validCFHarmonies,
				int[] validCFStepHarmonies,
				int[] validStartIndexes,
				int[] validEndIndexes,
				int[] invalidStepOutlines,
				int maxNoteRepeats,
				boolean parentSpecies) {
		
		this.name = name;
		this.speciesSystem = speciesSystem;
		this.validIntervals = validIntervals;
		this.validCFHarmonies = validCFHarmonies;
		this.validCFStepHarmonies = validCFStepHarmonies;
		this.validStartIndexes = validStartIndexes;
		this.validEndIndexes = validEndIndexes;
		this.invalidStepOutlines = invalidStepOutlines;
		this.maxNoteRepeats = maxNoteRepeats;
		this.parentSpecies = parentSpecies;
//		System.out.println("this:" + this.validCFStepHarmonies.length);
	}
}
