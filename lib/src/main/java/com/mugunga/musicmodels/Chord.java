package com.mugunga.musicmodels;
/**
 * Whereas the enum ChordType is the theoretical representation of a chord, the Chord class is a real applicable
 * instance of a chord. Once a Key is applied to a ChordType, the actual notes can be determined. 
 * 
 * @author laurencemarrin
 *
 */
public class Chord {
	private ChordType type;
	private Key baseKey;
	
	public Chord() {
		
	}
	
	public Chord(ChordType type, Key baseKey) {
		this.type = type;
		this.baseKey = baseKey;
	}
	
	public ChordType type() {
		return type;
	}
	
	public Key key() {
		return baseKey;
	}

	public void print() {
		//type.getChord();
		log("--------------");
		log("Chord: " + baseKey.name + type.shortDisplay + " :" + type.getChord());
		type.printInversions();
		log("--------------");
		
	}
	@Override
	public String toString() {
		return "" + baseKey.name + type.shortDisplay + "";
		
	}
	
	private void log(String msg) {
		System.out.println("Chord Log:         " + msg);
	}
	
	public Chord fromString(String chordName) {
		String chordKey; 
		String chordType;
		
		if(chordName.matches("\\wb.*") || chordName.matches("\\wb.*")) {
			chordKey = (String) chordName.subSequence(0, 2);
			chordType = chordName.substring(2);
			
		} else {
			chordKey = (String) chordName.subSequence(0, 1);
			chordType = chordName.substring(1);
		}

		l("chord" + chordName);
		l("chordKey: " + chordKey);
		l("chordType:" + chordType+"-");
		
			
		Key thisKey = Key.X;
		for (Key key : Key.values()) {
			log("key" + key.name);
			  if(chordKey == key.name) {
				  thisKey = key;
				  break;
			  }
		}
		ChordType thisType = null;
		for (ChordType type : ChordType.values()) {
			log("type" + type.longDisplay);
			if(chordType == type.shortDisplay) {
				thisType = type;
				break;
			}
		}
		
		return new Chord(thisType, thisKey);
		
		
	}
	
	private void l(String msg ) {
		System.out.println("Chord Log:        " + msg);
	}
}
