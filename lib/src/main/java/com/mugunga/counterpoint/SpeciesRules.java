package com.mugunga.counterpoint;
import java.util.ArrayList;
import java.util.List;

import com.mugunga.musicmodels.Interval;
import com.mugunga.musicmodels.IntervalIndexCollection;
import com.mugunga.musicmodels.Mode;

/**
 * This class contains all the rule checks for building the melody. SpeciesBuilder will call this class
 * many times, as it validates each index as a valid note to add to the melody. 
 * 
 * This set of rules assumes we are building modal melodies based on the indexes of the key:
 * 
 *      0 = Tonic
 * 		1 = second
 * 		2 = 3rd
 * 		-1 = leading tone
 * 		6 = leading tone
 * 
 * 
 * @author laurencemarrin
 *
 */
public class SpeciesRules {
	
	private static final int TRITONE_STEPS = Interval.TRITONE.steps;
	private static final int TRITONE_INDEXES = Interval.TRITONE.modeIndex;
	private static final int MELODIC_INDEX = 5;
	private static final int OCTAVE_STEPS = Interval.OCTAVE.steps; 
	private static final int PERFECT_FIFTH_STEPS = Interval.PERFECT_5TH.steps; 
	
	private final int CONTRARY_MOTION = 1;
	private final int OBLIQUE_MOTION = 2;
	private final int PARALLEL_MOTION = 3;
	private final int SIMILAR_MOTION = 4;

	public final SpeciesType speciesType;
	
	//Rules relating to climaxes:
	public final int[] validZenithIndexesPrimitive = {2, 4, 5, 7, 8, 9};
	public final int[] validNadirIndexesPrimitive = {-1, -2, -4, -5, -7, -9};
	private final double climaxBoundsThreshold = .2;
	
	//SpeciesType intervals
	public final int[] validIntervals;
	public final int[] validStartIndexes;
	public final int[] validEndIndexes;
	public final int[] invalidStepOutlines;
	
	//SpeciesSystem variables
	public final int maxMeasures;
	public final int minMeasures;
	public final int minLeaps;
	public final int maxLeaps;
	
	
	public SpeciesRules(SpeciesType speciesType){
		this.speciesType = speciesType;
		validIntervals = speciesType.validIntervals;
		validStartIndexes = speciesType.validStartIndexes;
		validEndIndexes = speciesType.validEndIndexes;
		invalidStepOutlines = speciesType.invalidStepOutlines;
		
		maxMeasures = this.speciesType.speciesSystem.maxMeasures;
		minMeasures = speciesType.speciesSystem.minMeasures;
		minLeaps = speciesType.speciesSystem.minLeaps;
		maxLeaps = speciesType.speciesSystem.maxLeaps;
	}

	/*
	 * Each melody must be confined to a range of notes
	 */
	public boolean checkTestNoteRange(int testIndex, NoteMelodyInProgress noteMelody) {
		//log("min Nadir index: " + notes.getMinNadirIndex());
		//log("max Zenith index: " + notes.getMaxZenithIndex());
		if (testIndex < noteMelody.getMinNadirIndex() || testIndex > noteMelody.getMaxZenithIndex()) {
			return false;
		}
		return true;
	}
	
	/*
	 * Determine if this note can be the second to last note (implying the next note will attempt to conclude the melody)
	 */
	public boolean checkAsPotentialPentultimate(NoteMelodyInProgress noteMelody, int testIndex,  int testInterval) {
		boolean pentultimateFound = false;
//		log("melody's valid pentultimates:" + melody.getValidPentultimates());
//		log("are we testing a valid Pentultimate:" + melody.getValidPentultimates().contains(testIndex));
		if(!noteMelody.getValidPentultimates().contains(testIndex)) {
//			log("valid pents doesn't contain my index");
			return false;
		}
		
		//does the melody not span the minimum range of notes?
		//TODO move minIndexRange to species type or system?
		if(!(noteMelody.zenith() - noteMelody.nadir() >= speciesType.speciesSystem.minIndexRange ||
			 testIndex - noteMelody.nadir() >= speciesType.speciesSystem.minIndexRange ||
			 noteMelody.zenith() - testIndex >= speciesType.speciesSystem.minIndexRange)){
//			log("Melody doesn't meet minimum range of: " + speciesType.speciesSystem.minIndexRange + " : " + melody.getAll());
			return false;
		}
		
		//does melody within the span of maximum range of notes?
		//TODO refactor mexIndexRange to different place. ALSO THIS SHOULD BE THE FINAL CHECK OF LAST NOTE. 
		if(noteMelody.zenith() - noteMelody.nadir() > speciesType.speciesSystem.maxIndexRange) {
			log("Melody range of " + (noteMelody.zenith() - noteMelody.nadir()) + "exceeds maximum range of " + speciesType.speciesSystem.maxIndexRange);
			return false;
		}
		
		//cannot approach pentultimate from leap greater than a 3rd.
		if (Math.abs(testInterval) > 2) {
//			log("Cannot approach pentultimate from leap > 3rd:" + testInterval);
//			logMelodies(melody);
			return false;
		}
		if(!checkClimaxesAsPotentialPentultimate(testIndex, noteMelody)) {
//			log("failing clamx check?");
			return false;
		}
		
		if(!minLeapHit(testIndex, noteMelody)) {
//			log("min leap fail" + noteMelody);
			return false;
		}
		
//		log(testInterval + " is the testInterval");
		if(testInterval == 0 && 
				(noteMelody.getMode() == Mode.AEOLIAN ||
				noteMelody.getMode() == Mode.DORIAN || 
				noteMelody.getMode() == Mode.MIXOLYDIAN)) {
//			log("Cannot have a unison going into a raised 7th voice");
			return false;
		}
		
		/*
		 * If we get this far, we have found a valid second-to-last note
		 */
		noteMelody.setPentultimateFound(true);
		
		return true;
		
	}
	
	/*
	 * Each melody must have a certain number of leaps, but not too many
	 */
	private boolean minLeapHit(int testIndex, NoteMelodyInProgress noteMelody) {
		int leapTally = noteMelody.leapTally();
		if(!(leapTally >= minLeaps || (leapTally == minLeaps - 1 && Math.abs(testIndex - noteMelody.getLast()) > 1))) {
//			log("leapTally: " + leapTally + "minLeaps:" + minLeaps + " melody: " + melody.getAll());
			return false;
		}
		return true;
	}

	
	public boolean checkClimaxesAsPotentialPentultimate(int testIndex, NoteMelodyInProgress noteMelody) {
		
		if(!minimumNoteCheck(noteMelody)) {
			log("Have not met minimum notes with only " + noteMelody.size() + " notes");
			return false;
		}
		//TODO : need to enforce that climax is on a strong downbeat!
		boolean validZenith = checkZenithAsPentultimate(testIndex, noteMelody);
		boolean validNadir = checkNadirAsPentultimate(testIndex, noteMelody);
//		log("valid Zenith:" + validZenith);
//		log("valid Nadir:" + validNadir);
		/*
		 * If both the Zenith and the Nadir are valid, the one with the greater magnitude takes precedent
		 */
		if(validZenith) {
			if(validNadir) {
				if(noteMelody.zenithMagnitude() >= noteMelody.nadirMagnitude()) {
					noteMelody.annealZenith();
				} else {
					noteMelody.annealNadir();
				}
			} else {
				noteMelody.annealZenith();
				//turn this on to force the zenith to be of greater magnitude than zenith
//				if(melody.zenithMagnitude() < melody.nadirMagnitude()) {
//					log("invalid Zenith but has larger magnitutde");
//					logMelodies(melody);
//					validZenith= false;
//				} else {
//					melody.annealZenith();				
//				}
			}
		} else if (validNadir) {
			if(noteMelody.zenithMagnitude() > noteMelody.nadirMagnitude()) {
//				log("invalid Zenith but has larger magnitutde");
//				logMelodies(melody);
				validNadir= false;
			} else {
				noteMelody.annealNadir();				
			}
		}
		
		return (validZenith || validNadir);	
	}

	/*
	 * Melody must have a certain number of notes or it just sounds like a fragment
	 */
	private boolean minimumNoteCheck(NoteMelodyInProgress noteMelody) {
		
		if(speciesType == SpeciesType.CANTUS_FIRMUS) {
			return noteMelody.size() < minMeasures - 2 ? false : true;
		} else if(speciesType == SpeciesType.FIRST_SPECIES) {
			return noteMelody.size() < noteMelody.getParentMelody().size() -2 ? false : true;
		} else {
			log("minimum note check not handled for this species type: " + speciesType.toString());
		}
		return true;
	}

	private boolean checkNadirAsPentultimate(int testIndex, NoteMelodyInProgress noteMelody) {
		//if the current pentultimate note is the highest note it will not be a valid zenith
//		log("is testIndex  of " + testIndex + " less than melody's nadir:" + melody.nadir());
		if (testIndex <= noteMelody.nadir()) {
			return false;
		}
		
		if(!noteMelody.uniqueNadir()) {
			return false;
		}
		int nIndex = noteMelody.nadir();
//		log("uniqueNadir of " + nIndex + " was found");
		if(!noteMelody.checkIfValidNadirIndex(nIndex)) {
			return false;
		}
//		log("Index of " + nIndex + "is Note a valid Nadir interval");
		int totalPotentialNotes = noteMelody.size() + 2;
		int nPos = noteMelody.nadirPos();
//		log("nPos:" + nPos + "totalPotentialNOtes: " + totalPotentialNotes + " JARG" + (double)nPos/(double)totalPotentialNotes);
		if ((double)nPos /(double)totalPotentialNotes >= 1 - climaxBoundsThreshold ||  
				(double)nPos / (double)totalPotentialNotes <= climaxBoundsThreshold) {
//				log("nadir fail: not centered:" + melody.toString());
				return false;
		}
		
		//Cannot have the same peak as parent
		if(speciesType == SpeciesType.FIRST_SPECIES) {
			if(noteMelody.getParentMelody().getClimaxPos() == nPos) {
//				log("nadir at same climax postition as parent melody: " + nPos);
				return false;
			}
		}
		return true;
	}

	private boolean checkZenithAsPentultimate(int testIndex, NoteMelodyInProgress noteMelody) {
//		log("is testIndex  of " + testIndex + " less than melody's zenith:" + melody.zenith());
		//if the current pentultimate note is the highest note it will not be a valid zenith
		if (testIndex >= noteMelody.zenith()) {
			return false;
		}
		
//		log("check for multiple zenith points");
		if(!noteMelody.uniqueZenith()) {
			return false;
		}
		int zIndex = noteMelody.zenith();
//		log("uniqueZenith of " + zIndex + " was found");
		//TODO should handle if 
		if(!noteMelody.checkIfValidZenithIndex(zIndex)) {
			return false;
		}
//		log("Index of " + zIndex + "is Note a valid Zenith interval");
		int totalPotentialNotes = noteMelody.size() + 2;
		int zPos = noteMelody.zenithPos();
		if ((double)zPos /(double)totalPotentialNotes >= 1 - climaxBoundsThreshold ||  
			(double)zPos / (double)totalPotentialNotes <= climaxBoundsThreshold) {
//			log("zenith fail: not centered:" + melody + ":::" + (double)zPos /(double)totalPotentialNotes);
			return false;
		}
		if(speciesType == SpeciesType.FIRST_SPECIES) {
//			log("species type is: " + speciesType.name());
//			log("check parent climax of:" + melody.getParentMelody().getClimaxPos());
			if(noteMelody.getParentMelody().getClimaxPos() == zPos) {
//				log("same climax postition as parent melody: " + zPos);
				return false;
			}
		}
		
		return true;
	}

	
	public boolean checkLeaps(NoteMelodyInProgress noteMelody, int testIndex) {
		int testInterval = testIndex - noteMelody.getLast();
//		log("about to check MAXX leap for itnerval " + testInterval);
		if(!maxLeapExceededCheck(noteMelody, testInterval)) {
//			log("max leap exceeded?");
			return false;
		}
		
		if(noteMelody.size() > 1) {
			if(!verifyLeaps(noteMelody, testInterval)) {
//				log("leap verification fail");
				return false;
			}
		}
		
		/*
		 *  Cannot have too many consecutive leaps
		 */
		if(noteMelody.size() > 2) {
			if(!consecutiveLeapsCheck(testInterval, noteMelody )) {
//				log("consecutive leaps check");
				return false;
			}
		}
		return true;	
		
	}

	/*
	 * Very specific rules about what intervals can be used as leaps, and in what direction, etc
	 * 
	 * Many are only applicable with certain size leaps, hence we check the Interval size before 
	 * even worrying about certain rules
	 */
	private boolean verifyLeaps(NoteMelodyInProgress noteMelody, int testInterval) {
//		(e) Octave and minor sixth leaps must be preceded and followed by a motion
//		in the opposite direction.
		int lastInterval = noteMelody.getLastInterval();
		switch(testInterval) {

		    case 3:
		    	if(lastInterval == 3) {
//		    		log("no two fourth leaps in a row; intervals:" + melody.getIntervals() + " testInterval: " + testInterval);
		    		return false;
		    	}
		    	break;
		    case -3:
		    	if(lastInterval == -3) {
//		    		log("no two fourth leaps in a row; intervals:" + melody.getIntervals() + " testInterval: " + testInterval);
		    		return false;
		    	}
		    	//TODO: verify if this even matters?
//		    	if(lastInterval == -4){
//		    		log("outline an octave, fifth must be on bottom" + melody.getIntervals() + " testInterval: " + testInterval);	
//		    		return false;
//		    	}
 		    	break;
			case 4:
				
				if(lastInterval >= 4) {
					//log("no two large leaps in the same direction"+ melody.getIntervals() + " testInterval: " + testInterval);
					return false;
				}
				//TODO verify if this even matters? 
//				if(lastInterval == 3) {
//					log("a fifth -> fourth jump must have the fifth be lower."+ melody.getIntervals() + " testInterval: " + testInterval);
//					return false;
//				}

				break;
			case -4: 
				
				if (lastInterval <= -4) {
//					log("no two fifths in the same direction"+ noteMelody.getIntervals() + " testInterval: " + testInterval);
					return false;
				}
				
				break;
			case 5: 
				if(lastInterval > 0) {
//					log("large interval up must be preceded by motion in the opposite direction" + melody.getIntervals() + " testInterval: " + testInterval);
					return false;
				}
				if(noteMelody.size() > 1 && lastInterval > 0) {
					log("leaps of a 6th must be preceded by downward motion"+ noteMelody.getIntervals() + " testInterval: " + testInterval);
					return false;
				}
				break;
			case -5:
				if(noteMelody.size() > 1 && lastInterval < 0) {
//					log("leaps of a 6th down must be preceded by upward motion"+ melody.getIntervals() + " testInterval: " + testInterval);
					return false;
				}
				break;
			case 7:
				if(noteMelody.size() > 1 && lastInterval > 0) {
//					log("leaps of a octave up must be preceded by downward motion"+ melody.getIntervals() + " testInterval: " + testInterval);
					return false;
				}
				break;
			case -7:
				if(noteMelody.size() > 1 && lastInterval < 0) {
//					log("leaps of a octave down must be preceded by upward motion"+ melody.getIntervals() + " testInterval: " + testInterval);
					return false;
				}
				break;
			default:
				break;
					
		}
		
		if (Math.abs(lastInterval) > 4 ) {
			//log("last leap greather than fifth, check stepwise motion" + melody.getAll() + " testInterval" + testInterval);
			//if leap interval greather than a 5th, must respond w/ stepwise direction in opposite motion
			if(lastInterval < 0 & testInterval != 1) {
//				log("Must follow large leap downward with step upward!" + melody.getAll() + " testInterval" + testInterval);
				return false;
			}
			if(lastInterval > 0 & testInterval != -1) {
				//log("Must follow large leap upward with step downward!" + melody.getAll() + " testInterval" + testInterval);
				return false;
			}
		}
		
		if(testInterval >= 4 && lastInterval <= -3 ) {
//			log("Cannot rebound from a semi-large leap with a greater leap in the opposire direction" + melody.getAll() + " testInterval" + testInterval);
			return false;
		}
		if(testInterval <= -4 && lastInterval >= 3 ) {
//			log("Cannot rebound from a semi-large leap with a greater leap in the opposire direction" + melody.getAll() + " testInterval" + testInterval);
			return false;
		}
		
		return true;
	}

	/*
	 * RULE: Cannot exceed maximum number of leaps in a row.
	 * TODO: Make # of leaps dynamic so it's based on a rules variable.
	 * @param testInterval
	 * @param noteMelody
	 * @return
	 */
	private boolean consecutiveLeapsCheck(int testInterval, NoteMelodyInProgress noteMelody) {
		
//		log("consecutive leaps check: " + melody.getAll());
		if ( Math.abs(testInterval) > 1 &&
			 Math.abs(noteMelody.getInterval(-2)) > 1 && 
		     Math.abs(noteMelody.getInterval(-1)) > 1) {
			//log("can only have two leaps in one direction. ");
			return false;				
		}
		
		//log("last Interval (m -1): " + melody.getInterval(-1));
		//log("last Interval (m -2): " + melody.getInterval(-2));
		if(testInterval < -1 && noteMelody.getInterval(-1) < -1 && noteMelody.getInterval(-2) < 0) {
//			log("two leaps must be preceded by motion in opposite direction:" + melody.getAll() + "testInterval: " + testInterval);
			return false;
		}
		if(testInterval > 1 && noteMelody.getInterval(-1) > 1 && noteMelody.getInterval(-2) > 0) {
//			log("two leaps must be preceded by motion in opposite direction" + melody.getAll() + "testInterval: " + testInterval);
			return false;
		}
			
		// TODO PRUNE THE NEXT NOTES AFTER TWO LEAPS SO THEY ONLY TEST UPWARD NOTES
		if (noteMelody.getInterval(-2) < -1 && noteMelody.getInterval(-1) < -1 && testInterval < 0) {
			return false;
		}
		//log("intervals: " + intervals.toString());
		// TODO PRUNE THE NEXT NOTES AFTER TWO LEAPS SO THEY ONLY TEST UPWARD NOTES
		if (noteMelody.getInterval(-2) > 1 && noteMelody.getInterval(-1) > 1 && testInterval > 0) {
//			log("two positive leaps must be followed by downward motion" + melody.getAll() + "testInterval: " + testInterval);
			return false;
		}
		
					
		if(Math.abs(testInterval) + Math.abs(noteMelody.getLastInterval()) == 6 &&
				Math.abs(testInterval + noteMelody.getLastInterval()) == 6) {
//			log("last interval: " + melody.getLastInterval());
//			log("cannot outline a seventh: " + melody.getAll() + " testInterval: " + testInterval);
//			log("parent emldoy: " + melody.getParentMelody());
			return false;				
		}
//			
//			return false;
//		}
		
		return true;
	}

	/*
	 *   RULE: Cannot exceeds the maximum number of leaps in a given melody
	 *   TODO: keep track of leaps as we go if it speeds up efficiency. Currently we are calculating as we go. 
	 */  
	private boolean maxLeapExceededCheck(NoteMelodyInProgress noteMelody, int testInterval) {
//		log("Checking max leaps (which is " + maxLeaps + ") exceeded: " + testInterval + "for melody: " + melody.toString() );
		if (noteMelody.size() > 1) {
			
//			log("Species rules test Interval:" + testInterval + " for " + melody.toString());
//			log("Math.abs(testInterval)" + Math.abs(testInterval));
//			log("melody.size():" + melody.size());

			if(noteMelody.size() > maxLeaps) {
//				log("Checking # of leaps:" + melody.leapTally() + "for interval:" + testInterval);
				if(noteMelody.leapTally() >= maxLeaps) {
					if(Math.abs(testInterval) > 1) {
//						log("failed leap check:" + melody.getAll().toString() + " testInterval: " + testInterval);
						return false;
					}
					//efficiency only, if we are too far from tonic note to return, cut the branch
					if(speciesType == SpeciesType.CANTUS_FIRMUS) {
						if(Math.abs(noteMelody.getLast() + testInterval - noteMelody.getFirst()) > 3) {
//							log("Too far from end tonic to walk back:" + melody.getAll() + " testInterval: " + testInterval + " maxNotes: " + maxNotes);
							return false;
						}
					}				
				}
			}
		}
		return true;
	}
	
	private void log(String msg) {
		System.out.println("Rules-Log:            " + msg);
	}

	public boolean validateTestInterval(int testInterval) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean validIntervalForSpecies(int testInterval) {	
		boolean vInterval = false;
		for(int i : this.validIntervals) {
			if(testInterval == i) {
				vInterval = true;
			}
		}
		return vInterval;
	}
	
	public boolean validEndIndexForSpecies(int testIndex) {
//		log("checking valid end index for species" + testIndex);
//		log("validEndIndexes :" + validEndIndexes);
		boolean vIndex = false;
		for(int i : this.validEndIndexes) {
			if(testIndex == i) {
				vIndex = true;
			}
		}
		return vIndex;
	}

	public boolean validTestInterval(NoteMelodyInProgress noteMelody, int testInterval, int testStepInterval) {
		if(!validIntervalForSpecies(testInterval)) {
//			log("interval " + testInterval + "not valid for species");
			return false;
		}
		
		if(isCantusFirmus() || isFirstSpecies()) {
			if(!directionChangeCheck(noteMelody.getIntervals(), testInterval)) {
//				log("direction has changed too much");
				return false;
			}	
		}
		
		if(Math.abs(testStepInterval) == TRITONE_STEPS ) {
//			log("Cannot jump a tritone");
			return false;
		}
		
		//TODO -> Disallow outlining a triTone, but make sure it doesn't actually sound cool before disabling. 
		
		return true;
	}

	private boolean isCantusFirmus() {
		if(speciesType == SpeciesType.CANTUS_FIRMUS) {
			return true;
		}
		return false;
	}

	private boolean directionChangeCheck(IntervalIndexCollection intervals, int testInterval) {
		//if meldoy changes direction more than X times in a row, BAD
		
		if(isCantusFirmus() && intervals.size() > 5) {
			if(testInterval > 0 && intervals.get(-1) < 0 && intervals.get(-2) > 0 && intervals.get(-3) < 0
			&& intervals.get(-4) > 0) {
				//log("Melody chaing direction too much " + intervals.toString());
				return false;
			} else if(testInterval < 0 && intervals.get(-1) > 0 && intervals.get(-2) < 0 && intervals.get(-3) > 0
					&& intervals.get(-4) < 0) {
				//log("Melody chaing direction too much " + intervals.toString());
				return false;
			}
		}
		
		//TODO refactor into a loop and instead of just 3 intervals in one direction, have it be X
		if(intervals.size() >= 4) {
			if(testInterval > 0 &&
					intervals.get(-1) > 0 &&
					intervals.get(-2) > 0 &&
					intervals.get(-3) > 0 &&
					intervals.get(-4) > 0) {
//				log("Cannot have more than 4 intervals in one direction " +testInterval + "intervals: " + intervals);
				return false;
			}
			if(intervals.size() >= 6) {
				if(Math.abs(testInterval) == 1 && 
						Math.abs(intervals.get(-1)) == 1 &&
						Math.abs(intervals.get(-2)) == 1 &&
						Math.abs(intervals.get(-3)) == 1 &&
						Math.abs(intervals.get(-4)) == 1 &&
						Math.abs(intervals.get(-5)) == 1 &&
						Math.abs(intervals.get(-6)) == 1) {
//					log("Cannot have this many steps in a row");
					return false;
				}
				
			}
				
			if(testInterval < 0 &&
					intervals.get(-1) < 0 &&
					intervals.get(-2) < 0 &&
					intervals.get(-3) < 0 &&
					intervals.get(-4) < 0) {
//				log("Cannot have more than 4 intervals in one direction " +testInterval + "intervals: " + intervals);
				return false;
			}
		}
		
		return true;
	}

	/*
	 * Cannot have too many repeated notes
	 */
	public boolean checkNoteRepetition(int testIndex, int lastIndex, int testInterval, NoteMelodyInProgress noteMelody) {
		//log("repetition check for: " + testIndex + " vs: " + lastIndex);
		if(testInterval == 0) {
			if(noteMelody.noteRepeatCount() == speciesType.maxNoteRepeats) {
//					log("exceeded max note repeats" + speciesType.maxNoteRepeats + " melodY: " + melody.getAll() + " testIndex" + testIndex);
				return false;
				
			}
		}
		
		/*
		 * Cannot have the same note too many times in a single melody, even if spaced out
		 */
		if(noteMelody.modeNotesTally(testIndex) >= speciesType.speciesSystem.maxNoteOccurrences) {			
//			log(" vv NoteIndex " + testIndex + " repeated too many times within melody " + melody.modeNotesTally(testIndex));
//			logMelodies(melody);
			return false;
		}
		
		if(isCantusFirmus() && 
				!noteMelody.finalNoteIsReady() && 
				testIndex%7 == noteMelody.getFirst() && 
				noteMelody.modeNotesTally(testIndex) == speciesType.speciesSystem.maxNoteOccurrences - 1  ) {
//			log("save one tonic tally for the end notes:" + melody.modeNotesTally(testIndex));
//			logMelodies(melody);
			return false;
		}
		
		if(!checkPatternViolations(noteMelody, testIndex, testInterval)) {
//			log("pattern check fail");
			return false;
		}
		
		
		return true;
	}
	
	//TODO make this so I can create a list of patterns (ABCABC, ABAB, that it will check the violations for)
	private boolean checkPatternViolations(NoteMelodyInProgress noteMelody, int testIndex, int testInterval) {
		//log("is First Species" + isFirstSpecies);
		//log("notes.size" + notes.size());

		//TODO enforce a certain standard deviation be present in the melody so we know it has enough movement
//		if (notes.size() >= 5) {
//			int[] stdCheck = {testNoteIndex, notes.get(notes.size()-1),notes.get(notes.size()-2),
//					notes.get(notes.size()-3),notes.get(notes.size()-4),notes.get(notes.size()-5) };
//			if (MathUtility.standardDeviation(stdCheck) <= stdCheckThreshold5) {
//				System.out.println("Last 5 notes have a standard deviation < " + stdCheckThreshold5 + " : " + stdCheck.toString());
//				return false;
//			}
//		}
		
		if (noteMelody.size() >= 3) {
			if( testIndex == noteMelody.get(-2) &&
			noteMelody.get(-1) == noteMelody.get(-3)	) {
//				log("NoteIndex " + testIndex + "  A - B - A - B pattern " + melody.getAll());
				return false;
			}
			
			if(testIndex == noteMelody.get(-1) && 
					testIndex == noteMelody.get(-3)) {
//				log ("Do not have an A b A <A> pattern" + melody.getAll()); 
				return false;
			}
			
			if(testIndex == noteMelody.get(-2) && 
					testIndex == noteMelody.get(-3)) {
//				log ("Do not have an A A b <A> pattern" + melody.getAll()); 
				return false;
			}
		}
				
		if(noteMelody.size() >= 4) {
			//no a b c b a pattern
//			if(testIndex == melody.get(-4)
//					&& melody.getLast() == melody.get(-3)) {
////				log(" No A - B - C - B - A pattern twice in one melody:" + melody.getAll() + " testIndex: " + testIndex);
//				return false;
//			}
			
			if (testIndex == noteMelody.get(-2) &&
					//testNoteIndex == notes.get(notes.size()	- 6) &&
					testIndex == noteMelody.get(-4)) {
//				log("A - x - A - x - A  repitition" + melody.getAll());
				return false;
			}
		}

		if (noteMelody.size() >= 5) {
			if(testIndex == noteMelody.get(-2) &&
				testIndex == noteMelody.get(-4)) {
				log("Cannot have B x B x B pattern:" + testIndex + noteMelody.getAll());
				return false;
			}
			
			if(testIndex == noteMelody.get(-3) &&
					noteMelody.get(-1) == noteMelody.get(-4) &&	
					noteMelody.get(-2) == noteMelody.get(-5)) {
//				log("no A B C A B C pattern: " + melody.getAll() + "testIndexX: " + testIndex);
				return false;
			}
			
		}
		
		if (noteMelody.size() >= 6) {
			if(testIndex == noteMelody.get(-6) &&
				noteMelody.get(-1) == noteMelody.get(-5) &&
				noteMelody.get(-2) == noteMelody.get(-4)) {
//				log("No A-B-C-D-C-B-A pattern:  " + testIndex + " : " + melody.getAll());
				return false;
			}
			
			if(testIndex == noteMelody.get(-4) &&
					noteMelody.get(-1) == noteMelody.get(-5) &&
					noteMelody.get(-2) == noteMelody.get(-6)) {
//				log("No A-B-C-D-A-B-C pattern:  " + testIndex + " : "+ melody.getAll());
				return false;
			}
		
		}
		

		if(isFirstSpecies() && noteMelody.size() >= 1) {
			
			NoteMelody parentMelody = noteMelody.getParentMelody();
			//log("Checking First Species repetition");
			if(testInterval == 0) {
				if(parentMelody.get(noteMelody.size() + 2) == testIndex) {
//					log("Cantus Firmus Will cause a triplet repeat F <F> C:" + testIndex + "for: " + parentMelody.getAll());
					return false;
				}
				//Check for C F <F>
				if(noteMelody.size() >= 2 && parentMelody.get(noteMelody.size()-1) == testIndex ) {
//					log("Cantus Firmus Will cause a triplet repeat C F <F>:" +testIndex + "for: " + parentMelody.getAll());
					return false;
				}
			}
			
			//Check for C <F> C
			if(parentMelody.get(noteMelody.size()) == testIndex &&
				noteMelody.size() <= parentMelody.size()-2 && parentMelody.get(noteMelody.size() + 2) == testIndex) {
//				log("Cantus Firmus Will cause a triplet repeat C F <F>:" +testIndex + "for: " + parentMelody.getAll());
				return false;
			}
			
					
			if(noteMelody.size() >= 2) {
				
				//Check for F C <F>
				if(parentMelody.get(noteMelody.size()) == testIndex &&
						noteMelody.get(-2)	 == testIndex) {
					log("Cantus Firmus Will cause a triplet repeat C F <F>:" +testIndex + "for: " + parentMelody.getAll());
					return false;
				}
				/**
				 *  1 2 3 2
				 *  7 8 9 <8>
				 * 
				 */
				if(testIndex == noteMelody.get(-2)) {
					if(parentMelody.get(noteMelody.size() + 1) == parentMelody.get(noteMelody.size()-1)) {
//						log("No A+B x+y A+<B> pattern" + testIndex + " for:" + melody.getAll() + " parent: " + parentMelody.getAll());
//						logMelodies((MelodyInProgress) melody);
						return false;
					}
				}
			}
				
		}		
		
		return true;
	}

	private boolean isFirstSpecies() {
		return speciesType == SpeciesType.FIRST_SPECIES ? true : false;
	}
	private boolean isSecondSpecies() {
		return speciesType == SpeciesType.SECOND_SPECIES ? true : false;
	}
	private boolean isThirdSpecies() {
		return speciesType == SpeciesType.THIRD_SPECIES ? true : false;
	}
	private boolean isFourthSpecies() {
		return speciesType == SpeciesType.FOURTH_SPECIES ? true : false;
	}
	
	public List<Integer> tailorStepIndexes(NoteMelody melody) {
//		log("tailoring Step Indexes...");
		List<Integer> stepIndexes = new ArrayList<>();
		int c = 0;
		int stepIndex = 0;
		//TODO RAISE FINAL BAR LOGIC APPLIED TO RAISE ANY INSTANCE IN FINAL BAR. REMOVING FOR NOW
		boolean raiseFinalBar = melody.size() > 11 ? true : false;
//		log("oldStepIndexes:" + melody.getStepIndexes());
//		log("melody size: " + melody.size());
		for(int i : melody.getStepIndexes().getAll()) {
//			log("checking for stepIndex: " + i);
//			log("melody.getInterval(-2)" + melody.getInterval(-2));
			c++;
			stepIndex = i;
			//stepIndex = melody.indexMap.get(i);
			if(isFirstSpecies() || isCantusFirmus()) {
				//melodic minor check on note before pentultimate. assume leading tone only 
			
//				if(raiseFinalBar && c > 8 && c < melody.size() - 1) {
//					
//					stepIndex = finalBarSpecialLogic(stepIndex, melody);
//				}else if(Math.abs(stepIndex - melody.getLastStepIndex()) == 4) {
//				if(Math.abs(stepIndex - melody.getLastStepIndex()) == 4) {
//					
////						log("would be raising the melodic minor if  had logic here tone");
////						switch(melody.getMode()) {
////						case AEOLIAN:    stepIndex++; break;
////						default: break;						
////						}
//				} else if(c == melody.size()-2 && melody.getInterval(-2) == 1) {
//				log("melody get -3 : " + melody.get(-2));
//				log("melody.get(-3)+77)%7: " + (melody.get(-2)+77)%7 + "c:" +c);
				if(c == melody.size()-2 && (melody.get(-3)+77)%7 == MELODIC_INDEX) {
//					log("should be melodic adjustment here...");
						switch(melody.getMode()) {
						case AEOLIAN: 
//							log("increment melodic?");
							stepIndex++; break;
						default: break;
						}
						//harmonic minor check
					} else if(c == melody.size()-1 && isFirstSpecies()) {
//						log("harmonic leading tone adjust...");
						switch(melody.getMode()) {
						case DORIAN:     
							stepIndex++; 
							melody.setRaisedLeadingTone(true);break;
						case MIXOLYDIAN: 
							stepIndex++; 
							melody.setRaisedLeadingTone(true);
							break;
						case AEOLIAN:    
							stepIndex++; 
							melody.setRaisedLeadingTone(true);
							break;
						default: break;
						}
					}		
			}
			stepIndexes.add(stepIndex);			
		}
//		log("newStepIndexes:" + stepIndexes);
		return stepIndexes;
		//		log("newStepIndexes:" + stepIndexes);
	
	}

	/*
	 * Not currently using the final bar logic because it doesn't sound right. Need to research
	 * what this rule really meant
	 */
	private int finalBarSpecialLogic(int stepIndex, NoteMelody melody) {
//		if(Math.abs(stepIndex - melody.getLastStepIndex()) == 2) {
////			log("raising  what should be the leading tones " + c + "th note, index:" + stepIndex);
//			switch(melody.getMode()) {
//			case DORIAN:     
//				stepIndex++; 
//				melody.setRaisedLeadingTone(true);
//			break;
//			case MIXOLYDIAN: 
//				stepIndex++; 
//				melody.setRaisedLeadingTone(true);
//				break;
//			case AEOLIAN:    
//				melody.setRaisedLeadingTone(true);
//				stepIndex++; 
//				break;
//			default: break;
//			} 
//		}
		return stepIndex;
	}

	/*
	 * A melody being built upon a primary melody must follow certain rules about not clashing notes, or having movement
	 * in certain directions
	 */
	public boolean checkAgainstParentMelody(NoteMelodyInProgress noteMelody, int testIndex, int testStepIndex, int testInterval) {
		//check for voice crossing.
//		log("check against parent melody....");
		if(isFirstSpecies()) {
//			log("checking for cross motion. ");
			//corresponding CF note to compare.
			int cfNote = noteMelody.getParentMelody().get(noteMelody.size() +1);
			int cfStepIndex = noteMelody.getParentMelody().getStepIndex(noteMelody.size() + 1);
			int harmony = Math.abs(cfStepIndex - testStepIndex);
			boolean upperVoice = noteMelody.isUpperVoice();
			boolean lowerVoice = noteMelody.isLowerVoice();
//			log("cfStepIndex: " + cfStepIndex + " testStepINdex" + testStepIndex);
//			log("all cf step indexes:" + melody.getParentMelody().getStepIndexes().getAll());
			
			if(noteMelody.size() == 0) {
				int cfNextIndex = noteMelody.getParentMelody().get(2);
				int cfStartIndex = noteMelody.getParentMelody().getFirst();
				
				if ((testIndex > cfStartIndex && testIndex <= cfNextIndex) ||
						(testIndex < cfStartIndex && testIndex >= cfNextIndex)) {
//					log("#$% CF will cross melody on second note.");
					logMelodies(noteMelody);
					return false;
				}
				
			}
			
			if(noteMelody.size() > 0) {
				int previousCfNote = noteMelody.getParentMelody().get(noteMelody.size());
				int previousNote = noteMelody.get(-1);
				if(upperVoice && testIndex <= previousCfNote) {
//					log(testIndex + " is lower than previousCFnote + " + previousCfNote + " in an uppder voice" );
					return false;
				} else if (lowerVoice && testIndex >= previousCfNote) {
//					log(testIndex + " is higher than previousCFnote + " + previousCfNote + " in an lower voice");
					return false;
				}
				
				int testMotionType = determineMotionType(noteMelody, testInterval);
//				log("parent melody:" + melody.getParentMelody().getAll());
				//log("testMotionType:" + testMotionType + " testInterval: " + testInterval);
				if((harmony == OCTAVE_STEPS || harmony == PERFECT_FIFTH_STEPS) &&
						(testMotionType == SIMILAR_MOTION || testMotionType == PARALLEL_MOTION)) {
//					log("Must only arrive at perfect consonance by oblique or contrary motion...parent: " + melody.getParentMelody().getIntervals());
//					log("melody:" + melody.getIntervals() + " testIndex: " + testInterval);
					return false;
				}
				
				//Cannot have a unison followed by an octave
				if(harmony == OCTAVE_STEPS && previousCfNote == previousNote) {
//					log("Don't follow a unison w an octave");
					return false;
				}
			
				if(noteMelody.size() < noteMelody.getParentMelody().size() - 1) {
					if(testIndex == cfNote) {
//						log("cannot have unison except on first and last note: " + testIndex + "vs cfnote: " + cfNote);
//						logMelodys(melody);
						return false;
					}
				}
				
				//melodic minor avoiding certain situations.
				if(noteMelody.getParentMelody().size() - 3 == noteMelody.size()) {
//					log("about to check for melodic minor consideratinos...");
					if(!checkForUnfortuitousMelodicMinor(noteMelody, testIndex, testInterval)) {
//						log("melodic minor issues, " + melody.getParentMelody().getAll() + " w tIndex:" + testInterval);
						return false;
					}
				} else if(noteMelody.getParentMelody().size() - 2 == noteMelody.size()) {
					if(!checkForLeadingToneIssues(noteMelody, testIndex, testInterval)) {
//						log("leading tone problem");
						return false;
					}
				}

				
				if(noteMelody.size() > 1) {
					if(Math.abs(testInterval) > 2 &&
							Math.abs(noteMelody.getLastInterval()) <= 1 &&
							Math.abs(noteMelody.getParentMelody().getInterval(noteMelody.size())) > 2 &&
							Math.abs(noteMelody.getParentMelody().getInterval(noteMelody.size() - 1)) == 1) {
//						log("do not have both melodies break: " + testInterval + "melody: " + melody);
//						log("parent Melody: " + melody.getParentMelody());
						return false;
						
					}
				}
			
			} 
			
//			log("next note cross test");
			if(noteMelody.size() < noteMelody.getParentMelody().size() - 1) {
				int nextCfNote = noteMelody.getParentMelody().get(noteMelody.size() + 2);	
				if(upperVoice && testIndex <= nextCfNote) {
//					log(testIndex + " is lower than nextCFnote + " + nextCfNote + " in an uppder voice");
					return false;
				} else if (lowerVoice && testIndex >= nextCfNote) {
//					log(testIndex + " is higher than nextCFnote + " + nextCfNote + " in an lower voice");
					return false;
				}
			}
			
			//cannot cross voices, need to add logic for not equalinf voices except on first note. 
			if(upperVoice) {
				if(testIndex < cfNote) {
//					log("Cannot cross voices during upperVoice:"+ testIndex + " vs: " + cfNote);
					return false;
				}
			}
			
			if(lowerVoice) {
				if(testIndex > cfNote) {
//					log("Cannot cross voices during lowerVoice:"+ testIndex + " vs: " + cfNote);
					return false;
				}
			}
			
			if(noteMelody.isTritoneResolutionNeeded()) {
//				log("NEED TO RESOLVE TRITONE WITH THIS INTERAL" + testIndex);
//				log("upperVoice?" + upperVoice);
//				log("lowerVoice?" + lowerVoice);
				if((upperVoice && testInterval != -1) ||(lowerVoice && testInterval != 1) ) {
//					log("this note: " + testInterval + "will not resolve the tritone so skip it");
					return false;
				}
			}
			//TODO -> need to check this separate from the others? Find a way to tell main melody to just resolve a Tritone
//			log("testStepIndex" + testStepIndex + "   cfStepIndex : " + cfStepIndex);
//			log("Math.abs((testStepIndex - cfStepIndex)%12 : " + Math.abs((testStepIndex - cfStepIndex)%12));
			
			if(Math.abs((testStepIndex - cfStepIndex)%12) == TRITONE_STEPS) {
				if(Math.abs(testIndex - cfNote)%7 != TRITONE_INDEXES) {
//					log("Can't have a TTRITONE that won't resolve to an inner third:");
					return false;
				}
//				log("Tritone Harmony: will the cantus Firmus resolve correctly...?" + testIndex);
//				logMelodies(melody);
				
				if(cfNote < testIndex) {
					if(noteMelody.getParentMelody().getInterval(1) != 1) {
//						log("tritone on  can't resolve because of cantus firmus");
//						logMelodies(melody);
						return false;
					}
				} else {
					if(noteMelody.getParentMelody().getInterval(1) != -1) {
//						log("tritone on can't resolve because of cantus firmus");
//						logMelodies(melody);
						return false;
					}
				}
				
				//log("Having a tritone interval that won't resolve on parent melody!!" + melody.getParentMelody());
				//log("melody: " + melody + " testStepINdex: " + testStepIndex);
				return true;
				//TODO shoudl this be false? 

			}
			
			
			boolean validStepHarmony = itemInIntArray(harmony, speciesType.validCFStepHarmonies);
			if(!validStepHarmony) {
//				log("harmony of " + harmony + "not contained in step harmonyes: " + speciesType.validCFStepHarmonies);
				//TODO refactor this modal exception out for checking it elsewhere
//				log("melody.getParentMelody().isLeadingToneRaised()" + melody.getParentMelody().isLeadingToneRaised());
				if(noteMelody.getParentMelody().isLeadingToneRaised() && noteMelody.size() > 8 
						&& noteMelody.size()< noteMelody.getParentMelody().size()-1) {
//					log("Melody is at correct size to check modal exception");
//					log("melody.getParentMelody().get(melody.size() + 1)%7"+ melody.getParentMelody().get(melody.size() + 1)%7);
					if(noteMelody.getParentMelody().get(noteMelody.size() + 1)%7 == 6 ||
							noteMelody.getParentMelody().get(noteMelody.size() + 1)%7 == -1) {
//						log("check modal exception");
						int newHarmony;
						switch(noteMelody.getMode()) {
						case DORIAN:     
							newHarmony =  Math.abs((cfStepIndex - 1) - testStepIndex);
							validStepHarmony = itemInIntArray(newHarmony, speciesType.validCFStepHarmonies);
							//melody.setRaisedLeadingTone(true);break;
						case MIXOLYDIAN: 
							//stepIndex++; 
							newHarmony =  Math.abs((cfStepIndex - 1) - testStepIndex);
							validStepHarmony = itemInIntArray(newHarmony, speciesType.validCFStepHarmonies);
							break;
						case AEOLIAN: 
							log("def should be AEOLIAN");
							newHarmony =  Math.abs((cfStepIndex - 1) - testStepIndex);
//							log("newHarmony" + newHarmony);
							validStepHarmony = itemInIntArray(newHarmony, speciesType.validCFStepHarmonies);
							break;
						default: break;
						} 
					}else if(noteMelody.getParentMelody().get(noteMelody.size() + 1)%7 == 5 ||
							noteMelody.getParentMelody().get(noteMelody.size() + 1)%7 == -2) {
						int newHarmony;
						switch(noteMelody.getMode()) {
						case AEOLIAN:    
							newHarmony =  Math.abs((cfStepIndex - 1) - testStepIndex);
							validStepHarmony = itemInIntArray(newHarmony, speciesType.validCFStepHarmonies);
							break;
						default: break;
						}
					}
				}
				//return false;
				
			}
			if(!validStepHarmony) {
				return false;
			}

		} else if(isSecondSpecies()) {
			//TODO add second species rules for this section
		}
//		log("passed all harmony checks");
		return true;
	}
	
	//important to note this occurs after the last note has already been added and it sets "resolution needed n it's own. 
	public boolean checkLastIndexAsTritone(NoteMelodyInProgress noteMelody, int testIndex, int testStep) {
		
		int harmony = (testStep - noteMelody.getParentMelody().getStepIndex(noteMelody.size()));
		int cfStepIndex = (noteMelody.getParentNote(noteMelody.size()));
//		log("cfStepIndex" + cfStepIndex);
		int stepHarmony = Math.abs(testIndex - cfStepIndex);
//		log("Harmony:" + harmony + "currentStepIndex + " + testIndex);
//		log("Step Index Different = " + stepHarmony);
		
		if(Math.abs(harmony) == TRITONE_STEPS) {
			if(stepHarmony != TRITONE_INDEXES) {
//				log("Must not have an actual triton interval but a diminished 5th that can resolve to a major third");
				return false;
			}
//			log("setting tritone resolution needed..." + harmony);
			noteMelody.setTritoneResolutionNeeded();
//			log("melody.getParentMelody().getInterval(1) :" +  melody.getParentMelody().getInterval(1));
			if(noteMelody.getParentMelody().getInterval(1) != 1 && 
					harmony == TRITONE_STEPS ) {
//				log("lower cantus firmus won't resolve the tritone, abort first sepcies!");
				logMelodies(noteMelody);
//				if(melody.size() == 1) {
//					melody.killMelody();
//				}
				return false;
			} else if(harmony == -6 && 
				noteMelody.getParentMelody().getInterval(1) != -1 ) {
//				log("upper cantus firmus won't resolve the tritone, abort first sepcies!");
				logMelodies(noteMelody);
//				if(melody.size() == 1) {
//					melody.killMelody();
//				}
				return false;
			}
		} 
//		else {
////			if(melody.size() == 1) {
////				melody.killMelody();
////			}
//			return false;
//		}
//		log("returning true?");
		return true;
		
	}
	
	private void logMelodies(NoteMelodyInProgress noteMelody) {
//		if(!isCantusFirmus()) {
//			log(" parentMelody: " + noteMelody.getParentMelody().getAll());
//		}
//		log(" working melody: " + noteMelody.getAll());
		
	}

	private boolean checkForLeadingToneIssues(NoteMelodyInProgress noteMelody, int testIndex, int testInterval) {
//		log("checking for leading TONNE situation" + testIndex + " " + (melody.getFirst() - 2));
		if((testIndex == noteMelody.getFirst() - 1)) {
			//log("melody.getParentMelody().get(-3)" + melody.getParentMelody().get(-3));
			
			if((noteMelody.getMode() == Mode.AEOLIAN  ||
					noteMelody.getMode() == Mode.DORIAN ||
					noteMelody.getMode() == Mode.MIXOLYDIAN) &&
					testInterval == 0) {
				
//				log("LEADING TONE nightmare:" + melody.parentMelody);
				return false;
			}
		}
		return true;
	}

	private boolean checkForUnfortuitousMelodicMinor(NoteMelodyInProgress noteMelody, int testIndex, int testInterval) {
//		log("checking for MELODDic situation" + testIndex + " " + (melody.getFirst() - 2));
		if(noteMelody.getMode() == Mode.AEOLIAN && (testIndex == noteMelody.getFirst() - 2)) {
//			log("melody.getParentMelody().get(-3)" + melody.getParentMelody().get(-3));
			
			if(noteMelody.getParentMelody().get(-3) == 2 ||
					testInterval == 0) {
//				log("modE:" + melody.getMode());
//				log("AELIAN nightmare:" + noteMelody.parentNoteMelody);
				return false;
			}
		}
		return true;
		
	}

	private boolean itemInIntArray(int harmony, int[] validCFStepHarmonies) {
		//log("is harmony" + harmony + " in validCFStepHarmonies" + validCFStepHarmonies.toString());
//		log("sepciesType:" + speciesType);
		//log(speciesType.validCFStepHarmonies.length + " is how big my intArray for ver is" + speciesType.validCFStepHarmonies.length);
		boolean validStepHarmony = false;
		int f = speciesType.validCFStepHarmonies.length;
		int i = 0;
		do {
//			log("checking harmony" + harmony +" against " + i + " value:" + speciesType.validCFStepHarmonies[i]);
//			log("checking:" + i + " value:" + speciesType.validCFStepHarmonies[i]);
			validStepHarmony = harmony == speciesType.validCFStepHarmonies[i] ? true : false;
			
			i++;	
		}while(!validStepHarmony && i < f);
//		for(int i : speciesType.validCFStepHarmonies) {
//			log("cfstep index: " + i);
//			if(harmony == i) {
//				validStepHarmony = true;
//			}
//		}
//		log("validStepHarmony can be true or false:" + validStepHarmony);
		return validStepHarmony;
	}

	public boolean validMotionCheck(NoteMelodyInProgress noteMelody, int testIndex, int testInterval ) {
		if(isFirstSpecies()) {
//			log("first species mothion");
			int testMotionType = determineMotionType(noteMelody, testInterval);
			List<Integer> motions = noteMelody.getMotions();
			if(motions.size()>2) {
//				log("motions are:" + motions.toString());
				//test for 3 intervals of same motion type. 
				if(testMotionType == PARALLEL_MOTION &&
						testMotionType == motions.get(motions.size()-1) &&
						testMotionType == motions.get(motions.size()-2)) {
//					log("Cannot have parallel motion for more than 2 intervals" + testIndex);
//					logMelodies(melody);
					return false;
				}
			}
			
			if(motions.size() >= 1) {
//				log("motion size >= 1");
				if(testMotionType == PARALLEL_MOTION && motions.get(motions.size() -1) == PARALLEL_MOTION) {
					
					if(testIndex == noteMelody.get(-2)) {
//					log("test index: " + testIndex + "melody.get(-2)" + melody.get(-2));
//						log("cannot do A - B - A in parallel motion: " + testIndex);
//						logMelodies(melody);
						return false;
					}
				}
			}
		} else if(isSecondSpecies()) {
			//TODO test motion from last downbeat? 
			//TODO test motion from halfnote?
		}
		
		return true;
	}
	
	int determineMotionType(NoteMelodyInProgress noteMelody, int interval) {
		int cfInterval = noteMelody.getParentMelody().getInterval(noteMelody.size());
		//log("cf Interval: " + cfInterval + "melody.size()" + melody.size());
		//log("testInterval:" + interval + "vs intrv: " + melody.getParentMelody().getIntervals());
		//log("testInterval:" + interval + "vs notes: " + melody.getParentMelody().getAll());
//		log("Parallel?" + (testInterval == cfInterval));
		if(interval == 0) {
			return OBLIQUE_MOTION;
		} else if((interval < 0 && cfInterval > 0) ||
		   (interval > 0 && cfInterval < 0)	) {
			return CONTRARY_MOTION;
		} else if(interval == cfInterval) {
			return PARALLEL_MOTION;
		} else {
			return SIMILAR_MOTION;
		}
	}

	public boolean checkPentultimateFound(NoteMelodyInProgress noteMelody, int testIndex, int testInterval) {
//		log("checking pentultimate...." + testIndex + " for " + melody.getAll() + " min notes: " + minNotes);
		//log("maxNotes:" + maxNotes);
		
		if(isCantusFirmus() && noteMelody.size() >= minMeasures - 2 && !noteMelody.finalNoteIsReady()) {	
//			log("inside check cantus");
			if(!checkAsPotentialPentultimate(noteMelody, testIndex, testInterval)) {
//				log("Pentultimate is breached, should be quitting...");
				//TODO verify assumes maxNotes for a child melody is set to the parent size??
				if(noteMelody.size() == maxMeasures - 2 && !noteMelody.finalNoteIsReady()) {
//					log("breaching max notes for CF, must be a penutltimate");
					return false;
				}
			} else {
//				log("checkPentultimateReady returned true");
			}
		} else if(isFirstSpecies() && noteMelody.size() == noteMelody.parentNoteMelody.size() - 2 && !noteMelody.finalNoteIsReady()) {
//			log("about to check 1S");
			if(!checkAsPotentialPentultimate(noteMelody, testIndex, testInterval)) {
//				log("must be pentultiamte for 1sr species here:");
				return false;
			}
			
		} else if(isSecondSpecies() && noteMelody.melodyLength() >= noteMelody.parentMelodyLength() - 1 && !noteMelody.finalNoteIsReady()) {
			if(!checkAsPotentialPentultimate(noteMelody, testIndex, testInterval)) {
				log("second species must be a pentultimate by this point (FAIL!): " + noteMelody.melodyLength());
				return false;
			}
		}
		return true;
	}
	
	public void setMelodyPentultimates(NoteMelodyInProgress melody) {
		if(isCantusFirmus()) {
			for (int i : validEndIndexes) {
				melody.addValidPentultimate(i+1);
//				log("validPentultimates." + melody.getValidPentultimates());
				
				//!!!turn this on to allow cantus firmus to approach from below
				//validPentultimates.add(i -1);
			}		
		} else if (isFirstSpecies()){
			for (int i : validEndIndexes) {
				//turn this on to allow cantus firmus to approach from below. 
				//validPentultimates.add(i + 1);
				melody.addValidPentultimate(i-1);
				//log("pentultimates are now:" + validPentultimates.toString());
			}
		}
	}

	public int maxIndexRange() {
		return speciesType.speciesSystem.maxIndexRange;
	}

	public boolean isConsonantHarmony(int firstNote, int secondNote ) {
		
		int interval = Math.abs(firstNote - secondNote)%7;
		for(int i : speciesType.speciesSystem.consonantIntervals) {
			if(interval == i) {
				return true;
			}
		}
		return false;
	}

	
	public boolean checkValid2SDownbeat(NoteMelodyInProgress noteMelody, int testIndex, int testInterval) {
		if(noteMelody.size() >= 2) {
			
			int lastDownbeat = noteMelody.get(-2); // a
			//int lastHalfbeat = melody.getLast(); // b
			int downbeatInterval = testIndex - lastDownbeat; // c - a
			int lastParentDownbeat = noteMelody.getParentNote(noteMelody.size()/2); //c
			log("last ParentDownbeat:" + lastParentDownbeat);
//		//dissonant passing tone rule
//		if(!isConsonantHarmony(testIndex, lastParentDownbeat)) {
//			if(!((downbeatInterval == 2 && testInterval == 1) || 
//					(downbeatInterval == -2 && testInterval == -1))) {
//				log("Dissonance passing tone must move by step in same direction: " + testIndex);
//				logMelodies(melody);
//				return false;
//			}
//		}
			
			log("testing against last downbeat:" + lastDownbeat);
			//TEST valid movements
			switch(downbeatInterval) {
			
			case 0:
				if(!(testInterval == 1 || testInterval == -1)) {
					log("Unison on DB must move by neighboring tone");
					return false;
				}
				break;
				
			case 1: 
				if(testInterval == -1) {
					break;
				}
				log("stepwise downbeats must progress with delay of melodic progression");
				return false;
				
			case -1: 
				if(testInterval == 1) {
					break;
				}
				log("stepwise downbeats must progress with delay of melodic progression");
				return false;
				
			case 2: 
				if(Math.abs(testInterval) == 1) {
					break;
				}
				log("A third leap must be approached by stepwise motion or jumping a fourth and falling back:" + testIndex);
				logMelodies(noteMelody);
				return false;
				
			case -2: 
				if(Math.abs(testInterval) == 1) {
					break;
				}
				log("A third leap must be approached by stepwise motion or jumping a fourth and falling back:" + testIndex);
				logMelodies(noteMelody);
				return false;
				
			case 3: 
				if(testInterval == 1 || testInterval == 2 || testInterval == -1) {
					break;
				} 
				//TODO DO I need to enforce dissonant passing tone here? 
				log("a downbeat leap of a fourth must be split by a second or third or change of register" + testIndex);
				return false;
				
			case -3: 
				if(testInterval == -1 || testInterval == -2 || testInterval == 1) {
					break;
				} 
				//TODO DO I need to enforce dissonant passing tone here? 
				log("a downbeat leap of a fourth must be split by a second or third or change of register" + testIndex);
				return false;
				
			case 4: 
				if(testInterval == 2 || testInterval == -1) {
					break;
				} 
				//TODO DO I need to enforce dissonant passing tone here? 
				log("a downbeat leap of a fifth must be split into thirds or approach by change of register" + testIndex);
				return false;
				
			case -4: 
				if(testInterval == 2 || testInterval == -1) {
					break;
				} 
				//TODO DO I need to enforce dissonant passing tone here? 
				log("a downbeat leap of a fifth must be split into thirds or approach by change of register" + testIndex);
				return false;
				
				
			case 5: 
				if(testInterval == 2 || testInterval == 3) {
					break;
				} 
				//TODO DO I need to enforce dissonant passing tone here? 
				log("a downbeat leap of a sixth must be split into a third and a fourth" + testIndex);
				return false;
				
			case -5: 
				if(testInterval == -2 || testInterval == -3) {
					break;
				} 
				//TODO DO I need to enforce dissonant passing tone here? 
				log("a downbeat leap of a sixth must be split into a third and a fourth" + testIndex);
				return false;
				
			case 6: 
				if(testInterval == -1) {
					break;
				} 
				//TODO DO I need to enforce dissonant passing tone here? 
				log("a downbeat leap of a seventh must be preceded by change of register" + testIndex);
				logMelodies(noteMelody);
				return false;
				
			case -6: 
				if(testInterval == 1) {
					break;
				} 
				//TODO DO I need to enforce dissonant passing tone here? 
				log("a downbeat leap of a seventh must be preceded by change of register" + testIndex);
				logMelodies(noteMelody);
				return false;
				
			case 7:
				log("no way to traverse a 7th between downbeats");
				logMelodies(noteMelody);
				return false;
				
			case -7:
				log("no way to traverse a 7th between downbeats");
				logMelodies(noteMelody);
				return false;
				
			default:
				log("UNHANLDED DOWNBEAT EXCEPTION");
				logMelodies(noteMelody);
				break;
			}
		}
		
		return true;
	}

	public boolean checkValid2SHalfbeat(NoteMelodyInProgress noteMelody, int testIndex, int testInterval) {
		int parentNote = (noteMelody.getParentNote((noteMelody.size()/2) + 1));
		log("parent note to test: " + parentNote);
		if(!isConsonantHarmony(testIndex, parentNote)) {
			log("found a dissonant halfbeat for second species: " + testIndex);
			if(Math.abs(testInterval) != 1) {
				log("dissonany passing tone must be approached by step " + testIndex + "melody: " + noteMelody.getAll());
			}
		}
		return true;
		
		//TODO is there anything else that needs to be checked?
	}
}
	
