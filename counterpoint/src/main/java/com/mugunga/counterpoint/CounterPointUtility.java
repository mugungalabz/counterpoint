package com.mugunga.counterpoint;

import java.util.HashMap;
import java.util.Map;

import org.jfugue.pattern.Pattern;

import com.mugunga.musicmodels.Mode;
import com.mugunga.musicmodels.StepIndexCollection;


public final class CounterPointUtility {
	public final String melodyStartIndex = "B4";
	private final int octaves = 8;
	private final String[] noteLetters = {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};
	public final Map<Integer, String> indexNoteLetterMap = new HashMap<Integer, String>();
	public final Map<String, Integer> noteLetterIndexMap = new HashMap<String, Integer>();
	public final int START_SPECIES_ON_REST = 1001;
	//public final static String CANTUS_FIRMUS = "Cantus Firmus";
	//public final String FIRST_SPECIES = "First Species";
	
	public CounterPointUtility() {
		
		for (int i = 0; i <= this.octaves; i++) {
			for (int j=1; j<= this.noteLetters.length; j++) {
				indexNoteLetterMap.put((i-1)*noteLetters.length + j, noteLetters[j - 1] + i);
				noteLetterIndexMap.put(noteLetters[j - 1] + i, (i-1)*noteLetters.length + j);
			}
		}
//		System.out.println("note letter size " + noteLetterMap.size());
//		for(int i = 1; i < noteLetterMap.size(); i++) {
//			System.out.println(noteLetterMap.get(i).toString());
//		}
	}
	
	public Pattern getMIDIPattern(NoteMelody melody, Mode mode, String startNote) {
		String pattern = "";
		int startIndex = noteLetterIndexMap.get(startNote.trim());
		//System.out.println("Starting Index:" + startIndex);
		
		switch(melody.getSpeciesType()) {
			case CANTUS_FIRMUS:
				
				for(int i: melody.getStepIndexes().getAll()) {
					pattern += indexNoteLetterMap.get(startIndex + i) + " ";
				}
				break;
			
			case FIRST_SPECIES:
				//int startIndex1 = noteLetterIndexMap.get(startNote.trim());
				StepIndexCollection fs = melody.getStepIndexes();
				//System.out.println("fs.string" + fs.toString());
				//System.out.println("cf.getnotes" + cf.getStepIndexes());
				
				for (int i = 1; i <= fs.size(); i++) {
					pattern += indexNoteLetterMap.get(startIndex + melody.getStepIndex(i - 1)) + "+";
					if(i == fs.size()) {
						pattern += indexNoteLetterMap.get(startIndex + fs.get(i - 1)) + " ";
					} else {
						pattern += indexNoteLetterMap.get(startIndex + fs.get(i - 1)) + " ";
					}
					System.out.println("pattern now: " + pattern);
				}
				break;
			
			case SECOND_SPECIES:
				//TODO Figure out how to write the Second Species with the MIDI library
				log("NEED TO CREATE SECOND SEPECIES MIDI PATTERN LOGIC");
				break;
			
			default: 
				System.out.println("Unhandled MIDI Pattern species Type");
				break;
		}
		
		//System.out.println("midi pattern: " + pattern);
		return new Pattern(pattern);
	}
	
	
	public String getMIDIString(NoteMelody melody, Mode mode, String startNote) {
		String pattern = "";
		int startIndex = noteLetterIndexMap.get(startNote.trim());
		//System.out.println("Starting Index:" + startIndex);
		
		switch(melody.getSpeciesType()) {
			case CANTUS_FIRMUS:
				
				for(int i: melody.getStepIndexes().getAll()) {
					pattern += indexNoteLetterMap.get(startIndex + i) + " ";
				}
			break;
			
			case FIRST_SPECIES:
				//int startIndex1 = noteLetterIndexMap.get(startNote.trim());
				StepIndexCollection fs = melody.getStepIndexes();
				StepIndexCollection parentMelodySteps = melody.getParentMelody().getStepIndexes();
				//log("fs.string" + fs.toString());
				//log("cf.getnotes" + cf.getStepIndexes());
				
				for (int i = 1; i <= fs.size(); i++) {
					pattern += indexNoteLetterMap.get(startIndex + parentMelodySteps.get(i)) + "+";
					pattern += indexNoteLetterMap.get(startIndex + fs.get(i)) + " ";
//					if(i == fs.size()) {
//						pattern += indexNoteLetterMap.get(startIndex + fs.get(i - 1)) + " ";
//					} else {
//						pattern += indexNoteLetterMap.get(startIndex + fs.get(i - 1)) + " ";
//					}
					//System.out.println("pattern now: " + pattern);
				}
			break;
			
			default: 
				System.out.println("Unhandled MIDI Pattern species Type");
			break;
		}
		
		//System.out.println("midi pattern: " + pattern);
		return pattern;
	}
	
	private void log(String msg) {
		System.out.println("Music Util-Log:       " + msg);
	}
	
}
