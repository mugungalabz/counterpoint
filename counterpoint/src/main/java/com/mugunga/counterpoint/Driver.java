package com.mugunga.counterpoint;

import com.mugunga.musicmodels.Mode;
import com.mugunga.musicmodels.NoteLength;

/** 
 * Create a CounterPointRunner instance, configure the run settings, and generate Classical Music. 
 *
 * Setting the base species count limits the number of melodies generated so the algorithm doens't run 
 * indefinitely
 *
 * Setting storeMelodies to 'true' will store the melodies in the relational database inaddition to outputting to 
 * MIDI
 * 
 * @author laurencemarrin
 *
 */
public class Driver {
	
	private static CounterPointRunner cpr;
	private static boolean testCF = false;
	private static boolean quitAfterCF = false;
	private static boolean test1S = false;
	private static boolean run1S = true;
	private static DBHandler dbHandler;
	private static boolean storeMelodies = true;
	
	private static int[] testBaseMelody =   {0, 2, 1, 3, 2, 4, 5, 4, 3, 1, 0 };  
	private static int[] test1SMelody  =  {0, 4, 6, 5, 6, 9, 7, 6, 5, 6, 7};  
	

	public static void main(String[] args) {
		
		cpr = new CounterPointRunner(SpeciesSystem.FUXIAN_COUNTERPOINT);
		if(testCF) {
			cpr.setTestBaseMelody(new TestMelody(testBaseMelody, NoteLength.WHOLE_NOTE));
		} else {
			cpr.setTargetBaseSpeciesCount(100);
		}
		if(test1S) {
			cpr.setTestFirstSpeciesMelody(new TestMelody(test1SMelody,NoteLength.WHOLE_NOTE));
		}
		cpr.setMode(Mode.LYDIAN);
		cpr.setRun1S(run1S);
		dbHandler = new DBHandler(storeMelodies);
		dbHandler.setup();
		cpr.setDBHandler(dbHandler);
		cpr.generateMusic();
		
		dbHandler.cleanup();
	}
	
}
