package com.bryanreinero.firehose.dao;

import java.util.Map;

public interface DAOService {
	Object execute ( String key, Map<String, Object> request ) throws DAOException;
	void setDataAccessObject( String key, DataAccessObject dao );


}
