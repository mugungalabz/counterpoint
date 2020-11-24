package com.mugunga.counterpoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.mugunga.musicmodels.JChops;
import com.mugunga.musicmodels.Mode;
/**
 * This class should receive the parameters needed to create the melodies. It needs to know whether
 * we are using Fuxian Counterpoint or some other SpeciesSystem, as well as the mode or any 
 * other pertinent information. It then creates the SpeciesBuilders and performs the recursive 
 * logic upon them to build out the music.
 * 
 * The CounterPointRunner can be told how many melodies to create, which will limit its processing time. 
 * If it is not told, it will continue running for a long time, until it has generated every Base Melody and
 * its First Species countermelodies that it thinks is valid. No human would be able to listen to every valid melody
 * in a single lifetime.
 * 
 * It should be able to be called by various drivers: Applications, Test Classes, etc.
 * 
 * @author laurencemarrin
 *
 */
public class CounterPointRunner {
	
	private boolean logging = true;
	
	private final  String MIDIdirectory = "MidiFiles/LoadTest/";
	private File csvout = new File("cantiFirmi.csv");
	private FileOutputStream csvfos = null;
	private BufferedWriter csvbw;
	private DBHandler dbHandler;
	private boolean storeInDB;
	
	private SpeciesSystem speciesSystem;
	private SpeciesType speciesType;
	private CounterPointStats stats;
	private Mode mode;
	private TestMelody testBaseMelody;
	private TestMelody testFirstSpeciesMelody;
	private boolean speciesGenerationComplete = false;
	private int baseSpeciesCount = 0;
	private int baseFailCount = 0;
	private int targetBaseSpeciesCount;
	private boolean test1S = false;
	private boolean run1S;
	private int cfW1s = 0;
	private List <CantusFirmus> generatedCantusFirmi = new ArrayList<>();
	
	/**
	 * Class Constructor
	 */
	public CounterPointRunner() {		
		
	}
	/**
	 * Class Constructor specifying which melody species system to use
	 * @param ss
	 */
	public CounterPointRunner(SpeciesSystem ss) {		
		this.speciesSystem = ss;
		if(speciesSystem == SpeciesSystem.FUXIAN_COUNTERPOINT) {
			speciesType = SpeciesType.CANTUS_FIRMUS;
		}
	}
	
	/**
	 *Once the CounterPointRunner is set up, we can start generating Classical Music melodies. 
	 *The SpeciesBuilder will do the heavy lifting, track the melody, call rules, and ultimately accept or reject
	 *which notes are added to our melody. The melodies are built recursively. Once a SpeciesBuilder is created, 
	 *we get a list of potential next notes. For each note, we enter the recursion.  
	 */
	public void generateMusic() {
		
		fileSetup();

		stats = new CounterPointStats();
		stats.logStartTime();
		stats.setMode(mode);
		SpeciesBuilder patientZero = new SpeciesBuilder(mode, speciesType, testBaseMelody);
		List<SpeciesBuilder> buildChain = new ArrayList<>();
		buildChain.add(patientZero);

		for(int i: patientZero.getNextValidIndexArrayRandomized()) {
			SpeciesBuilder cantusFirmusBuilder = new SpeciesBuilder(patientZero);
			if(cantusFirmusBuilder.checkAndSetFirstNote(i)) {
				buildChain.add(cantusFirmusBuilder);
				recursiveMelodySequencer(buildChain);				
			}
		}
		
		stats.logEndTime();
		stats.setBaseMelodies(baseSpeciesCount);
		stats.setBaseFailCount(baseFailCount);
		//stats.logStats();
		closeOutputFiles();
	}
	/**
	 * The list of SpeciesBuilders is simply the list of melody fragments up to this point. For Example: 
	 * 
	 * 		buildChain(0) = [A]
	 * 		buildChain(1) = [A, B]
	 * 		buildChain(2) = [A, B, G]
	 * 
	 *  And so the most recent element is our melody up to that point. Since there is so much information
	 *  stored at each step of the melody, we clone the entire SpeciesBuilder, and once we've explored that 
	 *  potential melody, we simply drop back to the last SpeciesBuilder and continue exploring that one's 
	 *  next valid notes.
	 *  
	 *  This means that left running indefinitely, this method would recursively discover every valid 
	 *  melody for the given set of constraints! (SpeciesRules). 
	 *  
	 *  Each SpeciesBuilder includes a list of potentially valid next indexes, and so for each of those 
	 *  indexes, we test as the next melody index. 
	 * 
	 * @param buildChain
	 */
	private void recursiveMelodySequencer(List<SpeciesBuilder> buildChain) {
		
		SpeciesBuilder currentCFB = buildChain.get(buildChain.size()-1);
		List<Integer> nextValidIndexes = currentCFB.getNextValidIndexArrayRandomized();

		for (int i : nextValidIndexes) {
			//log("Current cf: " + currentCFB.getNotes().toString() + " current testIndex: " + i);
			if (currentCFB.testAsNextIndex(i) && !speciesGenerationComplete) {
				SpeciesBuilder newCFB = new SpeciesBuilder(currentCFB);
				if (newCFB.addIntervalAndCheckForCompletion(newCFB.nextInterval) && !speciesGenerationComplete) {
					baseSpeciesCount++;
					processBaseSpecies(newCFB);
					if (baseSpeciesCount >= targetBaseSpeciesCount) {
						speciesGenerationComplete = true;
					}
				} else {
					buildChain.add(newCFB);
					recursiveMelodySequencer(buildChain);
				}
			} else {
				baseFailCount++;
			}
		}
		buildChain.remove(buildChain.size() - 1);	
	}
	
	/**
	 * When a valid base melody, or initial melody, or first melody is found, we process it. This includes
	 * storing it in the database, and then generating any additional melodies. In counterpoint, the 
	 * additional melody is referred to as the First Species of the base melody. Finally, we create a MIDI
	 * file which would include all children melodies. 
	 * 
	 * @param cf is the SpeciesBuilder containing a finalized melody or 'Cantus Firmus'
	 */
	private void processBaseSpecies(SpeciesBuilder cf) {
		CantusFirmus cfx = new CantusFirmus(cf, test1S);
		writeBaseSpecies(cfx);
		generatedCantusFirmi.add(cfx);

		if(run1S) {
			runFirstSpecies(cfx);
		}
		if(storeInDB) {
			writeMelodiesToDB(cfx);
		}
		cfx.createMIDIfile(MIDIdirectory, generatedCantusFirmi.size() + " Master");
	}
	/**
	 * Send the CantusFirmus to the dbHandler so it can be loaded into the database.
	 * @param cfx
	 */
	private void writeMelodiesToDB(CantusFirmus cfx) {
		
		dbHandler.insertCantusFirmus(cfx);
		if(run1S) {
			dbHandler.insertAllFirstSpecies(cfx);
		}
		
	}
	/**
	 * 
	 * @param cfx is the melody we are writing. 
	 */
	private void writeBaseSpecies(NoteMelody cfx) {
		try {
			csvbw.write(cfx.getNotesAsCSV());
		} catch (IOException e) {
			log("fail to write success stats ");
			e.printStackTrace();
		}
		try {
			csvbw.newLine();
		} catch (IOException e) {
			log("fail to create new line");
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate all the First Species melodies from the original melody. First Species is a specific term
	 * used in Fuxian Counterpoint to denote a set of rules this melody follows in relation to the Cantus 
	 * Firmus, or base melody. It does not mean just any old melody that sounds good. 
	 * 
	 *  We can test a specific first species melody by setting the childspecies test, otherwise the 
	 *  default will be to generate EVERY potential first species melody. 
	 *  
	 *  In the end we store it all to the db
	 * 
	 * @param cfx is the base melody, or 'Cantus firmus' we are using to create compatible 
	 * children melodies from
	 */
	private void runFirstSpecies(CantusFirmus cfx) {
		log("Generating first Species");
		cfx.setChildSpeciesTest(testFirstSpeciesMelody);
		cfx.generateSpecies(SpeciesType.FIRST_SPECIES);			
		
		log("First Species Created:************* " + cfx.getFirstSpeciesList().size());
		for(FirstSpecies fs : cfx.getFirstSpeciesList()) {
			log("FS : " + fs.getAll());
		}
		stats.tallyFirstSpecies(cfx.getFirstSpeciesList().size());
		if(cfx.getFirstSpeciesList().size() > 0) {
			cfW1s++;
		}
	}
	/**
	 *	The Mode of a melody is the scale used (Ionian, Dorian, etc)
	 * @return the Mode being used in this CounterPoint Runner
	 */
	public Mode getMode() {
		return mode;
	}
	/**
	 * Tell the CPR which musical scale (Mode) to use
	 * @param mode
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	public void setRun1S(boolean run1S) {
		this.run1S = run1S;
	}
	
	/**
	 * Target Base Species Count is when we want to limit how many base species to create
	 * so the program doesn't run forever and try to create every valid Cantus Firmus.
	 * @return
	 */
	public int getTargetBaseSpeciesCount() {
		return targetBaseSpeciesCount;
	}
	/**
	 * Limit the number of base species to be found so the program doesn't run indefinitely.
	 * @param targetBaseSpeciesCount
	 */
	public void setTargetBaseSpeciesCount(int targetBaseSpeciesCount) {
		this.targetBaseSpeciesCount = targetBaseSpeciesCount;
	}
	/**
	 * When testing a specific melody to see if it is a valid Cantus Firmus (or conversely, 
	 * to ensure our algorithm recognizes a melody that we think to be a valid Cantus Firmus)
	 * @param testBaseMelody
	 */
	public void setTestBaseMelody(TestMelody testBaseMelody) {
		this.testBaseMelody = testBaseMelody;
	}
	/**
	 * Test a specific melody as First Species. This really should only be used when also testing 
	 * a specific Cantus Firmus 
	 * 
	 * @param testFirstSpeciesMelody
	 */
	public void setTestFirstSpeciesMelody(TestMelody testFirstSpeciesMelody) {
		this.testFirstSpeciesMelody = testFirstSpeciesMelody;
		test1S = true;
	}
	/**
	 * How many total base species (Cantus Firmi) have been created by this CPRunner?
	 * @return
	 */
	public int getBaseSpeciesCount() {
		return baseSpeciesCount;
	}
	/** 
	 * How many First Species melodies have been created by this CPRunner?
	 * @return
	 */
	public int getFirstSpeciesCount() {
		return stats.getFirstSpeciesCount();
	}
	/** 
	 * Stats are used to monitor the health of the algorithm
	 * @return
	 */
	public CounterPointStats getStats() {
		return stats;
	}
	
	private void log(String msg) {
		if(logging) {
			System.out.println("CounterPointRunner Log:           " + msg);
		}
	}
	/**
	 * Directory setup for writing the MIDI files
	 * @throws IOException
	 */
	private void createMIDIDirectory() throws IOException {
		 File file = new File("MidiFiles");
		 if (file.exists() ) {
			 deleteFolder(file);
		 }
		 if (file.mkdir()) {
		     try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		 } else {
		     try {
				throw new IOException("Failed to create directory " + file.getParent());
			} catch (IOException e) {
				e.printStackTrace();
			}
		 }
	}
	
	public void deleteFolder(File file)
	    	throws IOException{
	 
	    	if(file.isDirectory()){
	 
	    		//directory is empty, then delete it
	    		if(file.list().length==0){
	    			
	    		   file.delete();
	    			
	    		}else{
	        	   String files[] = file.list();
	     
	        	   for (String temp : files) {
	        	      //construct the file structure
	        	      File fileDelete = new File(file, temp);
	        		 
	        	      //recursive delete
	        	     deleteFolder(fileDelete);
	        	   }
	        		
	        	   //check the directory again, if empty then delete it
	        	   if(file.list().length==0){
	           	     file.delete();
	        	   }
	    		}
	    		
	    	}else{
	    		//if file, then delete it
	    		file.delete();
	    	}
	}
	
	private void fileSetup() {
		
		 if (csvout.exists()){
		     csvout.delete();
		 }
		
		//.csv for statistical analysis
		try {
			csvfos = new FileOutputStream(csvout);
		} catch (FileNotFoundException e) {
			log("failed to create csv file output stream");
			e.printStackTrace();
		}
		csvbw = new BufferedWriter(new OutputStreamWriter(csvfos));
		
		try {
			createMIDIDirectory();
		} catch (IOException e1) {
			log("failed to create midi directory");
			e1.printStackTrace();
		}
	}
	
	private void closeOutputFiles() {

		try {
			csvbw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/** 
	 * Give the CPR a db to write to
	 * @param dbHandler
	 */
	public void setDBHandler(DBHandler dbHandler) {
		this.dbHandler = dbHandler;
		this.storeInDB = true;
	}
	/**
	 * How many valid First Species were generated for the first base melody.
	 * @return
	 */
	public int getFirstBaseMelodyFirstSpeciesCount() {
		log("1S count: " + generatedCantusFirmi.get(0).getfirstSpeciesCount());
		return generatedCantusFirmi.get(0).getfirstSpeciesCount();
	}
	/**
	 * Given a specific array of notes (a melody), determine if the first species list of the first Cantus Firmus 
	 * contains that as a melody. 
	 * @param test1sMelody
	 * @return
	 */
	public boolean firstSpeciesIncludes(int[] test1sMelody) {
		List<FirstSpecies> list = generatedCantusFirmi.get(0).getFirstSpeciesList();
		for(FirstSpecies fs : list) {
			if(JChops.compare(test1sMelody, fs.getAll())){
				return true;
			}
		}
		return false;
	}
	
}
