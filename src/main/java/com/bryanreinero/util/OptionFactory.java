package com.bryanreinero.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class OptionFactory {

	private interface Setter {
		public void set(OptionBuilder op, Object value);
	}

	private static enum Setters implements Setter {
		op {

			@Override 
			public String toString() { return "op";}
			
			@Override
			public void set(OptionBuilder op, Object value) {
				
				if ( !( value instanceof String ))
					throw new IllegalArgumentException( name+" can only be of type String");
				
				if (op == null)
					op = OptionBuilder.withArgName(value.toString());
				op.withArgName(value.toString());
			}

		},

		name {
			@Override 
			public String toString() { return "name";}
			
			@Override
			public void set(OptionBuilder op, Object value) {

				if ( !( value instanceof String ))
					throw new IllegalArgumentException( name+" can only be of type String");
				
				op.withLongOpt(value.toString());
			}
		},

		args {
			@Override
			public String toString() {
				return "args";
			}

			@Override
			public void set(OptionBuilder op, Object value) {

				Integer numArgs = null;
				try {
					if (value instanceof String) {
						if (((String) value).matches("multi"))
							op.hasArgs();
						else
							numArgs = Integer.parseInt((String) value);
						return;
					}

					if (value instanceof Integer)
						numArgs = (Integer) value;

					if (numArgs > 1)
						op.hasArgs(numArgs);

					else
						op.hasArg();

				} catch (Exception e) {
					throw new IllegalArgumentException(name
							+ " unrecognized format", e);
				}
			}
		},

		separator {
			@Override 
			public String toString() { return "separator";}
			
			@Override
			public void set(OptionBuilder op, Object value) {
				if(value instanceof Character )
					op.withValueSeparator( (Character)value );
				else
					if( value instanceof String ) {
						if( ((String)value).length() > 1  )
							throw new IllegalArgumentException(
									"separator must be 1 character");
						op.withValueSeparator( ((String)value).charAt(0) );
					}
			}
		},

		required {
			@Override 
			public String toString() { return "required";}
			
			@Override
			public void set(OptionBuilder op, Object value) {
				Boolean v = null;
				if( value instanceof Boolean )
					v = (Boolean)value;
				else
					v = Boolean.parseBoolean((String)value);
				
				op.isRequired(v);
			}
		},

		description {
			@Override 
			public String toString() { return "description";}
			
			@Override
			public void set(OptionBuilder op, Object value) {
				if( value instanceof String ) 
					op.withDescription((String)value);
				
				else
					throw new IllegalArgumentException( name+" can only be of type String" );
			}
		};

	}

	public static Options parseJSON( String json ) {
		 Options options = new Options();
		
		DBObject config = (DBObject) JSON.parse(json);
		DBObject ops = ((DBObject)config.get("options"));
		
		Iterator<Map<String, String>> it = ops.toMap().values().iterator();
		
		while(  it.hasNext() )  {
			Map o = it.next();
			options.addOption( 
					OptionFactory.getOption( o )
				);
		}
		return options;
	}
	
	public static String ingest( InputStream is ) throws IOException {
		Reader reader = new InputStreamReader(is, "UTF-8");
		StringBuffer sb = new StringBuffer();
		int data = reader.read();
		
		while(data != -1){
		    sb.append( (char)data );
		    data = reader.read();
		}

		reader.close();    
		return sb.toString();
	}

	public static Option getOption(Map<String, Object> params) {
		OptionBuilder builder = OptionBuilder.withArgName( "null" );
		
		List<Setters> sl = Arrays.asList(Setters.values());
		
		for( String key : params.keySet() )
			for( Setters s : sl )
				if( s.toString().compareTo(key) == 0 )
					s.set(builder, params.get(key));

		return builder.create();
	}
}
