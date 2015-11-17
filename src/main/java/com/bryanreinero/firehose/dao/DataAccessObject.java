package com.bryanreinero.firehose.dao;

public interface DataAccessObject <T> {
    Object execute( Object... args ) throws DAOException;
}
