package com.bryanreinero.firehose.dao;

import java.util.Map;

public interface DataAccessObject {
	public Object execute(Map<String, Object>request);
}
