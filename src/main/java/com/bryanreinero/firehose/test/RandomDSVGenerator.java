package com.bryanreinero.firehose.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import org.bson.types.ObjectId;

import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.cli.CommandLineInterface;


public class RandomDSVGenerator {
	
	private final static String appName = "RandomDSVGenerator"; 
	private int totalLines;
	private BufferedWriter bw = null;
	private char delimiter = ',';
	
	public RandomDSVGenerator ( String[] args ) { 
		
		CommandLineInterface cli = null;
		try {
			cli = new CommandLineInterface();
			cli.addOptions( appName );
			cli.addCallBack("f", new CallBack() {
			@Override
			public void handle(String[] values) {
				try { 
					bw = new BufferedWriter(new PrintWriter( values[0], "UTF-8"));
				}catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});

		cli.addCallBack("n", new CallBack() {
			@Override
			public void handle(String[] values) {
				totalLines = Integer.parseInt( values[0] );
			}
		});
		
		cli.addCallBack("d", new CallBack() {
			@Override
			public void handle(String[] values) {
				delimiter =  values[0].charAt(0) ;
			}
		});

		cli.parse( args );
		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit( -1 );
		}
	}


	public void execute() {
		Random rand = new Random();
		
		for( int i = 0; i < totalLines; i++ ) {
			
			StringBuffer buf = new StringBuffer( (new ObjectId()).toString() );
			buf.append( delimiter );
			buf.append(rand.nextFloat());
			buf.append( delimiter );
			buf.append(rand.nextFloat());
			buf.append( delimiter );
			buf.append(new Object());
			buf.append("\n");
			try {
				bw.write(buf.toString());
				bw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit( -1 );
			}
		}
	}
	
	public static void main(String[] args ) {
		RandomDSVGenerator gen = new RandomDSVGenerator( args );
		gen.execute();	
	}
}
