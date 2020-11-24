package com.mugunga.counterpoint;

import com.mugunga.counterpoint.CounterPointRunner;
import com.mugunga.counterpoint.CounterPointStats;
import com.mugunga.counterpoint.SpeciesSystem;
import com.mugunga.musicmodels.Mode;

/**
 * This Test class is for large batch monitoring of algorithm. When running many Cantus/Firmus, metrics like 
 * Standard Deviation, # of empty Cantus Firmus, # of First Species per Cantus Firmus, etc, should
 * remain steady. A large swing means something drastic happened to the algorithm.
 * 
 * @author laurencemarrin
 *
 */
public class FuxianCounterPointLoadMetricsTest {
	
	private Mode mode;
	private int targetCantusFirmusCount;
	private CounterPointRunner cpr;
	
	public FuxianCounterPointLoadMetricsTest() {
		mode = Mode.IONIAN;
		targetCantusFirmusCount = 250;
	}
	
	public FuxianCounterPointLoadMetricsTest(Mode mode) {
		this.mode = mode;
		targetCantusFirmusCount = 250;
	}
	
	public FuxianCounterPointLoadMetricsTest(Mode mode, int cantusFirmusCount) {
		this.mode = mode;
		targetCantusFirmusCount = cantusFirmusCount;
	}
	
	protected void run() {
		cpr = new CounterPointRunner(SpeciesSystem.FUXIAN_COUNTERPOINT);
		cpr.setMode(mode);
		cpr.setTargetBaseSpeciesCount(targetCantusFirmusCount);
		cpr.generateMusic();
	}

	public void setMode(Mode mode) {
		this.mode = mode;
		
	}

	public int getBaseMelodyCount() {
		
		return cpr.getBaseSpeciesCount();
	}

	public CounterPointStats getStats() {
		return cpr.getStats();
	}

	public void setBaseMelodyCount(int i) {
		cpr.setTargetBaseSpeciesCount(i);
		
	}


}
