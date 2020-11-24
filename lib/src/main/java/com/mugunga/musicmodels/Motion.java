package com.mugunga.musicmodels;

public enum Motion {
	PARALLEL("Parallel"),
	SIMILAR("Similar"),
	OBLIQUE("Oblique"),
	CONTRACTIVE("Contractive"),
	EXPANSIVE("Expansive"),
	MOTION("Motion");
	
	public final String motion;
	
	Motion(String name) {
		motion = name;
	}
}
