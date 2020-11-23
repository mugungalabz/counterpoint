package com.mugunga.counterpoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class CSVWriter {
	
	public static void writeCSV(ArrayList<String> csv, String fileName) {
		//Setup CSV Writing Classes
		FileOutputStream csvfos = null;
		BufferedWriter csvbw;
		File csvout = new File(fileName + ".csv");
		log("csv name: " + csvout.getAbsolutePath());
		try {
			csvfos = new FileOutputStream(csvout);
		} catch (FileNotFoundException e) {
			log("failed to create csv file output stream");
			e.printStackTrace();
		}
		csvbw = new BufferedWriter(new OutputStreamWriter(csvfos));
		
		//Write CSV
		for(int i = 0; i < csv.size();i++) {
			try {
				csvbw.write(csv.get(i));
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
		
		//Close File
		try {
			csvbw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void log(String msg) {
		System.out.println("CSVWriter Log:           " + msg);
	}
}
