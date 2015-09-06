package com.bryanreinero.firehose.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bryanreinero.firehose.metrics.SampleSet;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;

public class DAOServiceFactory {
	
	public static Logger logger = LogManager.getLogger( DAOServiceFactory.class.getName() ); 
	
	@SuppressWarnings("unchecked")
	public static DataAccessHub getDataAccessHub(String json, SampleSet set)
			throws DAOException {

		DBObject object = null;
		try {
			object = (DBObject) JSON.parse(json);
		} catch ( JSONParseException e ) {
			logger.warn("Can't build DataAccessHub. Trouble parsing JSON configuration."+e.getMessage() ); 
			throw new DAOException( "Building DataAccessHub failed.", e);
		}
		DataAccessHub hub = new DataAccessHub(set);

		for (Map<String, Object> clusterSpec : (List<Map<String, Object>>) object
				.get("clusters")) 
			hub.addCluster( (String)clusterSpec.get("name"), buildClient(clusterSpec) );

		for (Map<String, Object> daoSpec : (List<Map<String, Object>>) object
				.get("DAOs")) 
			configureNewDAO(daoSpec, hub );

		return hub;
	}
	
	@SuppressWarnings("unchecked")
	private static MongoClient buildClient( Map<String, Object> clusterSpec ) throws DAOException {
		
		String name = (String)clusterSpec.get("name");

		List<ServerAddress> addresses = new ArrayList<ServerAddress>();
		for( String host : (List<String>) clusterSpec.get("hosts") )
			try {
				addresses.add( new ServerAddress( host ) );
			} catch ( Exception e) {
				throw new DAOException("Failed to build MongoClient "+name, e);
			}

		return new MongoClient( addresses );
	}
	
	@SuppressWarnings("unchecked")
	private static void configureNewDAO( Map<String, Object> map, DataAccessHub hub  ) throws DAOException {
		
		MongoDAO dao = null;
		
		// Name
		String name = (String)map.get("name");
		if( name == null || name.isEmpty() )
			throw new IllegalArgumentException("DAO name undefined");
		
		// Name
		String className = (String)map.get("className");
		if( className == null || className.isEmpty() )
			throw new IllegalArgumentException("DAO className undefined");
				
		// namespace
		String namespace = (String)map.get("namespace");
		if( name == null || name.isEmpty() )
			throw new IllegalArgumentException("DAO namespace undefined");
		
		// Can instanciate the DAO at this point 
		// Name
		String clusterName = (String)map.get("cluster");
		if( clusterName == null || clusterName.isEmpty() )
			throw new IllegalArgumentException("Cluster name undefined");
		
		MongoClient cluster = null;
		if( ( cluster = hub.getCluster( clusterName ) ) == null )
			throw new DAOException("Error during DAO configuration. Referencing undefined cluster "+clusterName );
		
		Constructor<?> c = null;
		try {
			c = Class.forName(className).getConstructor( MongoClient.class, String.class );
		} catch (NoSuchMethodException e ) {
			logger.warn( "Error building dao "+name+". "+e.getMessage() );
			throw new DAOException ( "Error building dao "+name, e );
		} catch ( SecurityException e ) {
			logger.warn( "Error building dao "+name+". "+e.getMessage() );
			throw new DAOException ( "Error building dao "+name, e );
		} catch ( ClassNotFoundException e ) {
			logger.warn( "Error building dao "+name+". Could not find class "+e.getMessage() );
			throw new DAOException ( "Error building dao "+name, e );
		}
		
		try {
			dao = (MongoDAO) c.newInstance( cluster, namespace );
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			logger.warn( "Error building dao "+name+". "+e.getMessage() );
			throw new DAOException ( "Error building dao "+name, e );
		}
		
		//OPTIONS
		// WriteConcern
		setWriteConcerns ( (Map<String, Object>)map.get("writeConcern"), dao );
			
		// ReadPreference 
		setReadPreferences ( (Map<String, Object>)map.get("readPref"), dao );
		
		// Circuit Breaker
		setCircuitBreakers ( (List<Map<String, Double>>)map.get("circuitbreakers"), hub, name );
		
		hub.setDataAccessObject(name, dao);
	}
	
	private static void setWriteConcerns( Map<String, Object> concern, MongoDAO dao ) {
		dao.setW( concern.get("w").toString() );
	}
	
	private static void setReadPreferences( Map<String, Object> rp, MongoDAO dao ) {
		DBObject tags = (DBObject)rp.get( "tags" );
		
		ReadPreference pref = ReadMode.getPref( (String)rp.get( "mode" ), tags );
		dao.setReadPreference(pref);
	}
	
	
	// this interface needs to be re-thought, as does many aspects of this class
	private interface PrefGetter {
		ReadPreference getPref( DBObject tags ); 
	}
	
	private enum ReadMode {
		primary("primary", 
				new PrefGetter () {
			@Override
			public ReadPreference getPref(DBObject tags) {
				
				return ReadPreference.primary();
			}
		}), 
		secondary("secondary", new PrefGetter () {
			@Override
			public ReadPreference getPref(DBObject tags) {
				return ReadPreference.secondary();
			}
		}), 
		primaryPref("primaryPref", new PrefGetter () {
			@Override
			public ReadPreference getPref(DBObject tags) {
				return ReadPreference.primaryPreferred();
			}
		}), 
		secondaryPref("secondaryPref", new PrefGetter () {
			@Override
			public ReadPreference getPref(DBObject tags) {
				return ReadPreference.secondaryPreferred();
			}
		}),
		nearest("nearest", new PrefGetter () {
			@Override
			public ReadPreference getPref(DBObject tags) {
				return ReadPreference.nearest();
			}
		});
		
		private final String mode;
		private final PrefGetter getter;
		
		ReadMode( String mode, PrefGetter getter ) {
			this.mode = mode;
			this.getter = getter;
		}
		
		/**
		 * Get an instance of ReadPreference
		 * @param mode the type of ReadPreference to be returned
		 * @return
		 */
		public static ReadPreference getPref( String mode, DBObject tags ){
			for (ReadMode rm : ReadMode.values() )
				if( rm.mode.compareTo(mode) == 0)
					return rm.getter.getPref(tags);
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void setCircuitBreakers( List<Map<String, Double>> configObj,  DataAccessHub hub, String name  ) {
		
		for ( Map<String, Double> thresholds : configObj )
			for( Entry<String, Double> e : thresholds.entrySet() )
				hub.setCircuitBreaker( name, e.getKey(), e.getValue() );
	}

}
