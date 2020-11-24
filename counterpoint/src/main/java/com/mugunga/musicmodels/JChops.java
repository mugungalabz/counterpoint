package com.mugunga.musicmodels;

import java.util.List;

public class JChops {

	public static boolean compare(int[] intArray, List<Integer> intList) {
		if(intList.size() != intArray.length) {
			return false;
		}
		for(int i =0; i < intArray.length; i++) {
			if(intList.get(i) != intArray[i]) {
				return false;
			}
		}
		return true;
	}
	
	
}