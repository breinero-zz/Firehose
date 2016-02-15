package com.bryanreinero.firehose.util;

import java.io.Console;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Printer extends TimerTask {
	
	private final List<Object> printables = new ArrayList<Object>();
	private Console c = System.console();
	private static final char ESC = 27;
	private AtomicBoolean consoleMode = new AtomicBoolean(false);
	private AtomicBoolean cleared = new AtomicBoolean(false);
	
	private Timer timer = new Timer(false);
	private final int reportingPeriod;
	
	private PrintStream out = System.out;
	
	public Printer ( int seconds ) {
		this.reportingPeriod = seconds * 1000;
		setConsole(true);
	}
	
	public void addPrintable ( Object... args  ) {
		for( Object object : args ) 
			this.printables.add(object);
	}
	
	public void setPrintStream( PrintStream out ) {
		this.out = out;
	}
	
	public void setConsole ( boolean value ) {
		consoleMode.set( value && c != null );
	}
	
	public void start() {	
		timer.scheduleAtFixedRate( this, 0, reportingPeriod );
	}
	
	public void stop() {
		timer.cancel();
		run();
	}
	
	private class Indentor {
		int indentation = 0;
		String ident = "    ";
		public String format(String json ) {
			
			
			StringBuffer output = new StringBuffer();
			
			json = json.replaceAll("\\s+","").replaceAll(":", ": ");
			
			for ( int i = 0; i < json.length(); i++ ){
				char c = json.charAt(i);
				
				if ( c == '}' || c == ']' ) {
					output.append("\n\r");
					indentation--;
					
					for ( int t = 0; t < indentation; t++ )
						output.append( ident );
				}
				
				output.append(c);
				
				if ( c == '{' || c == '[' || c == ','  ) {
					
					if( c != ',' )
						indentation++;
					output.append("\n\r");
					
					for ( int t = 0; t < indentation; t++ )
						output.append( ident );
				}
			}
			return output.toString();
		}
	}

	@Override
	public void run() {

		Indentor id = new Indentor();
		StringBuffer buf = new StringBuffer();
		boolean isFirst = true;
		for (Object printable : printables) {
			
			if (isFirst)
				isFirst = false;
			else
				buf.append(", ");
			
			buf.append(printable);
		}
		
		String line = buf.toString();

		if ( consoleMode.get() ) {
			if (cleared.compareAndSet(false, true)) {
				c.writer().print(ESC + "[2J");
				c.flush();
			}
			c.writer().print(ESC + "[1;1H");
			c.flush();
			c.writer().println( id.format( line ) );
			c.flush();
		} else {
			out.println(buf.toString());
		}
	}
}
