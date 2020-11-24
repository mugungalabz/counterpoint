package com.mugunga.musicmodels;
import java.util.List;

public class IntervalIndexCollection extends MusicIntCollection  {
	
	public IntervalIndexCollection (List<Integer> intervals) {
		for(int i : intervals) {
			this.items.add(i);
		}
	}

	public IntervalIndexCollection(){}
	
	public int leapTally() {
		int leapTally = 0;
		for(int i: items) {
			if(Math.abs(i) > 1) {
				leapTally++;
			}
		}
		return leapTally;
	}
}
