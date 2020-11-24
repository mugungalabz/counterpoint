package com.mugunga.counterpoint;
public class FirstSpecies extends NoteMelody {
	
	private int dbID;
	
	public FirstSpecies(NoteMelodyInProgress noteMelodyInProgress) {
		super(noteMelodyInProgress);
		this.tailorStepIndexes();
	}

	public void setdbID(int dbID) {
		this.dbID = dbID;
	}


}
