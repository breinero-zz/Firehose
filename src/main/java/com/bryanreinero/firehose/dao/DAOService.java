package com.bryanreinero.firehose.dao;

import java.util.Map;

public interface DAOService {
	public Object execute ( String key, Map<String, Object> request ) throws DAOException;	
	public void setDataAccessObject( String key, DataAccessObject dao );
}
