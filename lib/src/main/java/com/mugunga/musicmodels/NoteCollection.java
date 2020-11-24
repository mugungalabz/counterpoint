package com.mugunga.musicmodels;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mugunga.musicmodels.NoteLength;

public abstract class NoteCollection implements Iterable<Note> {
	
	protected List<Note> notes = new ArrayList<>();
	
	public NoteCollection() {
		
	}
	
	public NoteCollection(ArrayList<Note> notes) {
		this.notes = notes;
	}
	
	public void setNotes(int[] indexes, double[] lengths) {
		if(indexes.length != lengths.length) {
			log("mismatch in number of notes:" + indexes + " lengths: " + lengths);
		} else {
			for(int i = 0; i < indexes.length; i++) {
				addNote(new Note(indexes[i], lengths[i] ));
			}
		}
	}
	
	public void setUniformLengthMelody(int[] indexes, NoteLength length) {
		for(int i : indexes) {
			addNote(new Note(i, length));
		}
	}
	
	public void addNote(Note note) {
		notes.add(note);
	}
	
	/*
	 * This method has some important assumptions:
	 * 
	 * 1) Generally we are looking back from the most recent notes. So if we send a parameter -3, it means we want the 3rd note
	 * back from what we currently have. All negative parameters are looking backward
	 * 
	 * 2) Positive requests n will return the nth note, since the index starts at 0, we add 1 to the index.
	 * 
	 * 3) requesting the 0th note just returns the 0th index, or the first note. 
	 */
	public int get(int index) {
//		log("getting index of:" + index + " from " + notes.size() + " notes");
		if(index > 0) {
			return notes.get(index - 1).index();
		} else if(index < 0) {
			return notes.get(notes.size() + index).index();
		} else if (index == 0) {
			return notes.get(0).index();
		}
		return 911;
	}
	
	public void add(Note item) {
		notes.add(item);
	}
	
	public void addIfNotDuplicate(Note item) {
		if(!notes.contains(item)) {
			add(item);
		}
	}
	
	public void addAllNotes(NoteCollection notes) {
		this.notes.addAll(notes.getAllNotes());
	}
	
	public List<Note> getAllNotes() {
		return notes;
	}
	
	public List<Integer> getAll() {
		List<Integer> getNotes = new ArrayList<>();
		for(Note note : notes) {
			getNotes.add(note.index());
		}
		
		return getNotes;
	}
	
	public Iterator<Note> iterator() {
		return notes.iterator();
	}
	
	public int size() {
		return notes.size();
	}
	
	public int getLast() {
		return notes.get(notes.size() - 1).index();
	}
	
	public int getFirst() {
		return notes.get(0).index();
	}
	
	public String toString() {
		return notes.toString();
	}
	
	boolean empty() {
		return notes.size() == 0 ? true : false;
	}
	
	private void log(String msg) {
		System.out.println("MusicIntColl-Log:     " + msg);
	}
	
	public String getNotesAsCSV() {
		return notes.toArray().toString().replaceAll("[\\[\\]]", "");
	}
}
