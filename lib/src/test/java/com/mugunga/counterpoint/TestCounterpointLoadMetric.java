package com.mugunga.counterpoint;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.mugunga.musicmodels.Mode;


class TestCounterpointLoadMetric {
	
	private FuxianCounterPointSingleMelodyTest cpt;
	private CounterPointStats stats;
	
	/*TODO
	 * 
	 * Create assertions for the following:
	 * 
	 * Standard Deviation of CFs
	 * Mean 1S per CFs
	 * % of empty CFs
	 * Max 1S per CFs
	 * 
	 * 
	 */
	
	@Disabled
	void phyrgianLoadTest() {
		int baseMelodyCount = 1000;
		FuxianCounterPointLoadMetricsTest cpt = new FuxianCounterPointLoadMetricsTest(Mode.PHYRGIAN, baseMelodyCount);
		cpt.run();
		stats = cpt.getStats();
		
		//Ensure we are creating exactly as many base Melodies as we think we are
		assertEquals(stats.baseMelodyCount(), baseMelodyCount);
		
		//Ensure the # of firstSpecies create per each Base Species is in a proper range
		//TODO store these min/max type variables in a metrics class or something and use them
		assertTrue(stats.firstSpeciesperBaseSpecies() > 13 && stats.firstSpeciesperBaseSpecies() < 35);
		assertTrue(stats.baseSpeciesSuccessRate() > .0045 && stats.baseSpeciesSuccessRate() < .075 );
		
	}
	
	@Disabled
	void ionianLoadTest() {
		int baseMelodyCount = 100;
		FuxianCounterPointLoadMetricsTest cpt = new FuxianCounterPointLoadMetricsTest(Mode.IONIAN, baseMelodyCount);
		cpt.run();
		stats = cpt.getStats();
		
		//Ensure we are creating exactly as many base Melodies as we think we are
		assertEquals(stats.baseMelodyCount(), baseMelodyCount);
		
		//Ensure the # of firstSpecies create per each Base Species is in a proper range
		//TODO store these min/max type variables in a metrics class or something and use them
		assertTrue(stats.firstSpeciesperBaseSpecies() > 25 && stats.firstSpeciesperBaseSpecies() < 80);
		assertTrue(stats.baseSpeciesSuccessRate() > .0045 && stats.baseSpeciesSuccessRate() < .0175 );
		
	}
	
	@Disabled
	void locrianLoadTest() {
		int baseMelodyCount = 100;
		FuxianCounterPointLoadMetricsTest cpt = new FuxianCounterPointLoadMetricsTest(Mode.LOCRIAN, baseMelodyCount);
		cpt.run();
		stats = cpt.getStats();
		
		//Ensure we are creating exactly as many base Melodies as we think we are
		assertEquals(stats.baseMelodyCount(), baseMelodyCount);
		
		//Ensure the # of firstSpecies create per each Base Species is in a proper range
		//TODO store these min/max type variables in a metrics class or something and use them
		assertTrue(stats.firstSpeciesperBaseSpecies() > 10 && stats.firstSpeciesperBaseSpecies() < 80);
		assertTrue(stats.baseSpeciesSuccessRate() > .0045 && stats.baseSpeciesSuccessRate() < .0175 );
		
	}
	
	@Test
	@Disabled
	void massLoadTest() {
		int baseMelodyCount = 100;
		ArrayList<String> csvStats = new ArrayList<String>();
		for(int i = 1; i <= Mode.values().length; i++) {
			Mode m = Mode.values()[i-1];
			log("Mode: " + m);
			int fsCounter = 0;
			for(int j = 0; j < 200; j++) {
				FuxianCounterPointLoadMetricsTest cpt = new FuxianCounterPointLoadMetricsTest(Mode.values()[i-1],baseMelodyCount);
				
				cpt.run();
				fsCounter += cpt.getStats().getFirstSpeciesCount();
				//csvStats.add(cpt.getStats().toCSV());
				if (j%50 == 0) {
					log("j " + j);
				}
			}
			csvStats.add(m.modeName + "," + fsCounter);
		}
		
		CSVWriter.writeCSV(csvStats, "CounterPointLoadTest");
		
	}
	
	/*
	 * TODO problem: Why is Locrian getting low First Species counts?
	 *      hypothesis: too many tritone violations centered around the root
	 *      solution: determine if this is true, and whether this mode should actually allow tritones
	 */
	@Test
	void locrianMassLoadTest() {
		int baseMelodyCount = 10;
		ArrayList<String> csvStats = new ArrayList<String>();
		for(int j = 0; j < 100; j++) {
			FuxianCounterPointLoadMetricsTest cpt = new FuxianCounterPointLoadMetricsTest(Mode.LOCRIAN,baseMelodyCount);
			cpt.run();
			csvStats.add(cpt.getStats().toCSV());
		}
		CSVWriter.writeCSV(csvStats, "CounterPointLoadTest");
	}
	
	
	@Test
	void ionianMassLoadTest() {
		int baseMelodyCount = 10;
		ArrayList<String> csvStats = new ArrayList<String>();
		for(int j = 0; j < 100; j++) {
			FuxianCounterPointLoadMetricsTest cpt = new FuxianCounterPointLoadMetricsTest(Mode.IONIAN,baseMelodyCount);
			cpt.run();
			csvStats.add(cpt.getStats().toCSV());
		}
		CSVWriter.writeCSV(csvStats, "CounterPointLoadTest");
	}
	
	public void log(String msg) {
		System.out.println("LoadTest Log:   " + msg );
	}
	
	
	
	

}
