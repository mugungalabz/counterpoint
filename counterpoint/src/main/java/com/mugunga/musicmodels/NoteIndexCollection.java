package com.mugunga.musicmodels;
import java.util.List;


public class NoteIndexCollection extends MusicIntCollection{
	
	public NoteIndexCollection() {
		
	}
	
	//This constructor deliberately does not use the add() method of parent class because it will trigger a bunch of uneeded logic
	public NoteIndexCollection(List<Integer> notes) {
		for(int i : notes) {
			items.add(i);
		}
	}
 }
