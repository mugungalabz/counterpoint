package com.mugunga.counterpoint;

import com.mugunga.counterpoint.CounterPointRunner;
import com.mugunga.counterpoint.SpeciesSystem;
import com.mugunga.counterpoint.TestMelody;
import com.mugunga.musicmodels.Mode;
import com.mugunga.musicmodels.NoteLength;
/**
 * This is basically a wrapper class for single-melody testing via CounterPointRunner
 * 
 * @author laurencemarrin
 *
 */
public class FuxianCounterPointSingleMelodyTest {
	
	private CounterPointRunner cpr;
	
	public FuxianCounterPointSingleMelodyTest() {
		cpr = new CounterPointRunner(SpeciesSystem.FUXIAN_COUNTERPOINT);
	}

	public void testMelody() {
		cpr.generateMusic();
	}	
	

	public void setTestCantusFirmus(int[] testCFMelody) {
		cpr.setTestBaseMelody(new TestMelody(testCFMelody, NoteLength.WHOLE_NOTE));
		cpr.setTargetBaseSpeciesCount(1);
	}

	public void setTestFirstSpecies(int[] test1SMelody) {
		cpr.setTestFirstSpeciesMelody(new TestMelody(test1SMelody, NoteLength.WHOLE_NOTE));
	}

	public void setMode(Mode mode) {
		cpr.setMode(mode);
	}

	public boolean validCantusFirmus() {
		return cpr.getBaseSpeciesCount() == 1 ? true : false;
	}
	
	public boolean validFirstSpecies() {
		return cpr.getFirstSpeciesCount() == 1 ? true : false;
	}

	public int getFirstSpeciesCountForSingleBaseMelody() {
		return cpr.getFirstBaseMelodyFirstSpeciesCount();
	}

	public boolean firstSpeciesIncludes(int[] test1sMelody) {
		return cpr.firstSpeciesIncludes(test1sMelody);
	}
}
