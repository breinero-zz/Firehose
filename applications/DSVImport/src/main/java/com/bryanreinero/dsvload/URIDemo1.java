package com.bryanreinero.dsvload;// URIDemo1.java

import java.net.*;

class URIDemo1
{
    public static void main (String [] args) throws Exception
    {
        if (args.length != 1)
        {
            System.err.println ("usage: java URIDemo1 uri");
            return;
        }

        URI uri = new URI (args [0]);

        System.out.println ("Authority = " +
                uri.getAuthority ());

        System.out.println ("Fragment = " +
                uri.getFragment ());

        System.out.println ("Host = " +
                uri.getHost ());

        System.out.println ("Path = " +
                uri.getPath ());

        System.out.println ("Port = " +
                uri.getPort ());

        System.out.println ("Query = " +
                uri.getQuery ());

        System.out.println ("Scheme = " +
                uri.getScheme ());

        System.out.println ("Scheme-specific part = " +
                uri.getSchemeSpecificPart ());

        System.out.println ("User Info = " +
                uri.getUserInfo ());

        System.out.println ("URI is absolute: " +
                uri.isAbsolute ());

        System.out.println ("URI is opaque: " +
                uri.isOpaque ());
    }
}
