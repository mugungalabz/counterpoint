package com.mugunga.counterpoint;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBHandler {
	private Connection dbConnection = null;
	private final String JDBCURL = "jdbc:mysql://localhost:3306/mugunga?useSSL=false";
	private final String DBUSER = "gituser";
	private final String DBPASSWORD = "gituser1";
	private final String TIMEZONE = "UTC";
	private boolean storeMelodies = true;
	
	public DBHandler(boolean storeMelodies) {
		this.storeMelodies = storeMelodies;
	}
	
	void setup() {
		
	      try {
	         Properties info = new Properties();
	         info.setProperty("user", DBUSER);
	         info.setProperty("password", DBPASSWORD);
	         info.setProperty("serverTimezone", TIMEZONE);
	         System.out.println("Connecting to database: "+JDBCURL);
	         dbConnection=DriverManager. getConnection(JDBCURL, info);
	         System.out.println("Connection is successful.");
	         Statement stmt = dbConnection.createStatement();
	         stmt.close();
	      }
	      catch(Exception e) {
	    	 this.storeMelodies = false;
	         e.printStackTrace();
	      }
	}
	
	void cleanup() {
		try {
			dbConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	/**
	 * inser
	 * @param cfx
	 */
	public void insertCantusFirmus(CantusFirmus cfx) {		
		if(storeMelodies) {
			Statement q;
			String query = "INSERT INTO mugunga.cantus_firmi (melody, mode_id) "
					+ "VALUES (? , ?)";
			PreparedStatement ps;
			String melody = cfx.getStepIndexesAsCSV();
			log("inserting melody into DB: " + melody);
			try {
				ps = dbConnection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, melody);
				ps.setInt(2, cfx.getModeID());
				ps.executeUpdate();

				ResultSet rs = ps.getGeneratedKeys();
				rs.next();
				int cfxID = rs.getInt(1);
				cfx.setdbID(cfxID);
				System.out.println("Have created CF # " + cfxID);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Insert Cantus Firmus, originally with a hard coded query
	 * @param cfx
	 */
	@Deprecated
	public void insertCantusFirmusRawQuery(CantusFirmus cfx) {		
		if(storeMelodies) {
			Statement q;
			try {
				q = dbConnection.createStatement();
				q.executeUpdate("INSERT INTO mugunga.cantus_firmi (melody, mode_id) "
						+ "VALUES ('" + cfx.getStepIndexesAsCSV() + "' , " + cfx.getModeID() + ")", 
						Statement.RETURN_GENERATED_KEYS);
				ResultSet rs = q.getGeneratedKeys();
				rs.next();
				int cfxID = rs.getInt(1);
				cfx.setdbID(cfxID);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Insert all the First Species melodies, indexed with their Cantus Firmus as their primary 
	 * melody. Use Prepared Statement Batch Execution. 
	 */
	public void insertAllFirstSpecies(CantusFirmus cfx) {
		if(storeMelodies) {
			int cantusFirmusID = cfx.getDBid();
			log("Storing first species melodies for Cantus Firmus #" + cantusFirmusID);
			PreparedStatement ps;
			String query = "INSERT INTO MUGUNGA.FIRST_SPECIES (cantus_firmus_ID, melody) "
					+ "VALUES (? , ?)";
			try {
				ps = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				cfx.getFirstSpeciesList().forEach(firstSpecies -> {
					try {
						ps.setInt(1, cantusFirmusID);
						ps.setString(2, firstSpecies.getStepIndexesAsCSV());
						ps.addBatch();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				});
				ps.executeBatch();
				ResultSet rs = ps.getGeneratedKeys();
				//TODO tie the IDs to the First Species Melodies so we can eventually tie Second Species Melodies
				//to them as well. 
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
	}
	/**
	 * Insert all First Species. 
	 * @param cfx
	 */
	@Deprecated
	public void insertAllFirstSpeciesForCantusFirmus(CantusFirmus cfx) {
		if(storeMelodies) {
			int cantusFirmusID = cfx.getDBid();
			log("Storing first species melodies for Cantus Firmus #" + cantusFirmusID);
			Statement q;
			try {
				q = dbConnection.createStatement();
				cfx.getFirstSpeciesList().forEach( firstSpecies ->
				insertFirstSpecies(cantusFirmusID, firstSpecies, q));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Insert an individual First Species melody Note, this will be Deprecated
	 * @param cantusCirmusID
	 * @param firstSpecies
	 * @param q
	 */
	private void insertFirstSpecies(int cantusCirmusID, FirstSpecies firstSpecies, Statement q) {
		try {
			int fsID = q.executeUpdate("INSERT INTO MUGUNGA.FIRST_SPECIES (cantus_firmus_ID, melody) "
					+ "VALUES (" + cantusCirmusID + " , '" + firstSpecies.getStepIndexesAsCSV() + "')", 
					Statement.RETURN_GENERATED_KEYS);
			firstSpecies.setdbID(fsID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void log(String msg) {
		System.out.println("DBLog:                " + msg);
	}
	
}
