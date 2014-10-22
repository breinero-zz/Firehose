package com.bryanreinero.firehose.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class OptionFactory {

	private interface Setter {
		public void set(Option op, Object value);
	}

	private static enum Setters implements Setter {
		longOpt {

			@Override 
			public String toString() { return "longOpt";}
			
			@Override
			public void set(Option op, Object value) {
			
		
				if ( !( value instanceof String ))
					throw new IllegalArgumentException( op+" can only be of type String");
				
				op.setLongOpt(value.toString());
			}

		},

		name {
			@Override 
			public String toString() { return "name";}
			
			@Override
			public void set(Option op, Object value) {

				if ( !( value instanceof String ))
					throw new IllegalArgumentException( name+" can only be of type String");
				
				op.setArgName(value.toString());
			}
		},

		args {
			@Override
			public String toString() {
				return "args";
			}

			@Override
			public void set(Option op, Object value) {

				Integer numArgs = null;
				try {
					if (value instanceof String) {
						if (((String) value).matches("multi"))
							op.setArgs( Option.UNLIMITED_VALUES );
						else
							numArgs = Integer.parseInt((String) value);
						return;
					}

					if (value instanceof Integer)
						numArgs = (Integer) value;

					if (numArgs >= 1)
						op.setArgs(numArgs);

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
			public void set(Option op, Object value) {
				if(value instanceof Character )
					op.setValueSeparator( (Character)value );
				else
					if( value instanceof String ) {
						if( ((String)value).length() > 1  )
							throw new IllegalArgumentException(
									"separator must be 1 character");
						op.setValueSeparator( ((String)value).charAt(0) );
					}
			}
		},

		required {
			@Override 
			public String toString() { return "required";}
			
			@Override
			public void set(Option op, Object value) {
				Boolean required = null;
				if( value instanceof Boolean )
					required = (Boolean)value;
				else
					required = Boolean.parseBoolean((String)value);
				
				op.setRequired(required);
			}
		},

		description {
			@Override 
			public String toString() { return "description";}
			
			@Override
			public void set(Option op, Object value) {
				if( value instanceof String ) 
					op.setDescription((String)value);
				
				else
					throw new IllegalArgumentException( name+" can only be of type String" );
			}
		};
		
		public static Setter get( String key ) {
			
			List<Setters> sl = Arrays.asList(Setters.values());
			
			for( Setters s : sl )
				if( s.toString().compareTo(key) == 0 )
					return s;
			
			return null;
		}

	}

	public static Options parseJSON( String json ) {
		 Options options = new Options();
		
		DBObject config = (DBObject) JSON.parse(json);
		List<DBObject> ops = (List<DBObject>)config.get("options");
		
		for( DBObject opt : ops ) {
			if( opt.containsField("op") == false )
				throw new IllegalArgumentException("opt field required");
			
			Option option = new Option( (String)opt.get("op"), null);
			opt.removeField("op");
			
			for( String name : opt.keySet() )
				Setters.get( name ).set(option, opt.get(name) );
				
			options.addOption( option);
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

	public static void main ( String[] args ) {
		String json = "{ ops: [] } ";
		OptionFactory.parseJSON(json);
	}
}
