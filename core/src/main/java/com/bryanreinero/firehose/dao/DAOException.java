package com.bryanreinero.firehose.dao;

public class DAOException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DAOException( String message, Exception cause ) {
		super( message, cause );
	}
	
	public DAOException( String message ) {
		super( message );
	}
}
