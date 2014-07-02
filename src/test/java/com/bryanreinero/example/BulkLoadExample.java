package com.bryanreinero.example;

import java.util.Map;
import java.util.LinkedHashMap;

import org.junit.Test;

import com.bryanreinero.firehose.Converter;

class BulkLoadExample {
	
	private static final String delimeter = ",";
	private static final Map<String, String> header;
	private static final String testLine = "532ed20af217e2d2fc1b9543,bryan,42,3.50,5.550";
	
	static {
		// initialize conversion map
		header = new LinkedHashMap<String, String>();
		header.put("_id", "objectid");
		header.put("name", "string");
		header.put("count", "int");
		header.put("cost", "float");
		header.put("average", "double");
	}
	
	@Test
	public static void main( String[] args ) {
		Converter converter = new Converter(header, delimeter );
		
		System.out.println( converter.convert( testLine ) );
	}
	
	
}