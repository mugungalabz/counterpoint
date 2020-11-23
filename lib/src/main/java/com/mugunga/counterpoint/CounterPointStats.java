package com.mugunga.counterpoint;

import com.mugunga.musicmodels.Mode;
/**
 * CounterPointStats helps track various metrics related to the Cantus Firmus algorithm. The metrics can tie into
 * unit testing and ensure code changes do not drastically alter the results, or if they do, give cause for 
 * investigation.
 * 
 * Once the algorithm starts receiving social feedback, the stats can be regressed against the social feedback
 * to determine which types of melodies are more pleasing, and then used to bias the algorithm towards making better
 * and better melodies. 
 * 
 * @author laurencemarrin
 *
 */
public class CounterPointStats {
	
	private long startTime;
	private long endTime;
	private int totalBaseMelodies;
	private int baseFailCount;
	private int totalFirstSpeciesMelodies;
	private Mode mode;
	/**
	 * Class constructor
	 */
	public CounterPointStats() {
		totalBaseMelodies = 0;
		totalFirstSpeciesMelodies = 0;
	}

	public void logStartTime() {
		this.startTime = System.currentTimeMillis();
	}
	
	public void logEndTime() {
		this.endTime = System.currentTimeMillis();
	}
	/**
	 * What is the average # of counter melodies found per base melody. 
	 * @return
	 */
	public double firstSpeciesperBaseSpecies() {
		return (double)totalFirstSpeciesMelodies/(double)totalBaseMelodies;
	}
	
	public int baseMelodyCount() {
		return totalBaseMelodies;
	}
	
	public int getFirstSpeciesCount() {
		return totalFirstSpeciesMelodies;
	}
	
	public void tallyFirstSpecies(int firstSpecies) {
		totalFirstSpeciesMelodies += firstSpecies; 
	}
	
	public void setBaseMelodies(int baseMelodies) {
		totalBaseMelodies += baseMelodies; 
	}
	
	public void logStats() {
		long totalTime = endTime-startTime;
		log("totalTime:" + totalTime);
		double CF1s = (double)totalFirstSpeciesMelodies/(double)totalBaseMelodies;
		log("1S per CF" + CF1s);
		log(" Success Total: " + totalBaseMelodies);
		log(" Failtotal" + baseFailCount);
		log("Success Rate " + (double)totalBaseMelodies/(double)baseFailCount);
	}
	
	private static void log(String msg) {
		System.out.println("Stats Log:           " + msg);
	}

	public void setBaseFailCount(int baseFailCount) {
		this.baseFailCount = baseFailCount;
	}
	/**
	 * Ratio of base melodies that actually successfully find counter melodies
	 * @return
	 */
	public double baseSpeciesSuccessRate() {
		return (double)totalBaseMelodies/(double)baseFailCount;
	}
	
	public void setMode(Mode m) {
		this.mode = m;
	}

	public String toCSV() {
		return mode.toString() + "," + totalBaseMelodies + "," + totalFirstSpeciesMelodies;
	}

}
