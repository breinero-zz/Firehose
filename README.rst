========
Firehose
========

:Description: A tool import for DSV files into MongoDB.
:Author: Bryan Reinero <breinero@gmail.com>

Overview 
========

Firehose is both a mulithreaded DSV import tool for MongoDB, AND a instrumented execution framework which you can use to benchmark your own applications.

Firehose includes these major components:
 - A simple code instrumentation and reporting `library <https://github.com/bryanreinero/Firehose/tree/master/src/main/java/com/bryanreinero/firehose/metrics>`_
 - A customizable command line interface `builder <https://github.com/bryanreinero/Firehose/tree/master/src/main/java/com/bryanreinero/firehose/cli>`_
 - A multithreaded `worker pool <https://github.com/bryanreinero/Firehose/blob/master/src/main/java/com/bryanreinero/util/WorkerPool.java>`_
 - An application `framework <https://github.com/bryanreinero/Firehose/blob/master/src/main/java/com/bryanreinero/util/Application.java>`_ so that you may use all of these components together for your own load testing purposes 

The Main Take Away
~~~~~~~~~~~~~~~~~~

While Firehose does include a DSV import tool, that functionality is actually just an example application which uses all of the components together to do useful work. Let's take a closer look at the import tool to understand how you might want to use Firehose's features.

Firehose by Example: The DSV Import Tool
----------------------------------------

Ok, so I want to read a CSV file and import those records into MongoDB as fast as I can. To get that file into MongoDB I will need to execute a three step process on each line of the file;

- Read the next line from the file
- Parse the line, converting it into a object prepped for insertion
- Insert the new object into MongoDB

As a curious and conscientious software engineer, I am very interested to know how much time each of these steps so that I can establish performance baselines. I can use Firehose's instrumentation library to mark the start end of each step with use of the `Interval class <https://github.com/bryanreinero/Firehose/blob/master/src/main/java/com/bryanreinero/firehose/Firehose.java#L76>`_ class. For instance, here's how I determine how long and individual insertion took

::

    Interval insertDuration = samples.set("insert"); // Set the time marker
    dao.insert( object ); // perform the actual work
    insertDuration.mark(); // mark the operation as complete

The insertDuration Interval is collected and averaged automatically by Firehose giving me mean latency of the insertion operation. Firehose will then pretty-print the running average to the console so that I may see how fast my operations are executing in real time.

Firehose pretty prints this output, ( refreshing the console each second)

::

 {
    threads: 2,
    "linesread": 100000,
    samples: {
        units: "microseconds",
        "interval": 1000000,
        ops: [
            {
                name: "total",
                count: 7926,
                average: 250
            },
            {
                name: "readline",
                count: 7926,
                average: 1
            },
            {
                name: "insert",
                count: 7926,
                average: 182
            },
            {
                name: "build",
                count: 7924,
                average: 3
            }
        ]
    }
 }

This output tells me that inserts are taking an average of 182 microseconds, as averaged over a time interval of 1000000 microseconds, (1 second). During this 1 second interval I inserted 7926 documents. As the output is printed in JSON format I can insert these stats into MongoDB for benchmarking analysis!

You can take a look at how this workload is processed `here <https://github.com/bryanreinero/Firehose/blob/master/src/main/java/com/bryanreinero/firehose/Firehose.java#L35>`_

Firehose by Example: The DSV Import Command Line Interface
----------------------------------------------------------

Under the hood, Firehose uses the `Apache Commons CLI library <http://commons.apache.org/proper/commons-cli/>`_ to parse command line options passed in at runtime. Firehose wraps the Commons CLI into the framework such that we can configure my own set of command line options easily. Using the CLI framework is a two step process.

1. Declare my command line options in a properties file
#. Assign callback methods to handle the input

As an example let's take a look at the usage for Firehose's DSV Import feature uses the Commons CLI:  

Usage
-----

.. list-table::
   :header-rows: 1
   :widths: 10,25,20,90

   * - **option**
     - **long form**
     - **type**
     - **description**
   * - -cr
     - --noPretty
     -        
     - print out in CR-delimited lines. Default is console mode pretty printing (when possible)
   * - -f,
     - --file 
     - <filepath>               
     - filename to import
   * - -fs,
     - --fsync 
     -                   
     - write concern: wait for page flush
   * - -h,
     - --headers 
     - <name:type>         
     - ',' delimited list of columns
   * - -j,
     - --journal
     -                
     - enable write concern wait for journal commit
   * - -m,
     - --mongos 
     - <host:port>           
     - ',' delimited list of mongodb host to connect to. Default localhost:27017,
   * - -ns,
     - --namespace 
     - <namespace>    
     - target database and collection this work will use
   * - -pi,
     - --printInterval  
     - <seconds>
     - print output every n seconds
   * - -ri,
     - --reportInterval
     - <seconds>        
     - average stats over an time interval of i milliseconds
   * - -t,
     - --threads 
     - <threads>         
     - number of worker threads. Default 1
   * - -v,
     - --verbose
     -            
     - Enable verbose output
   * - -wc,
     - --writeConcern 
     - <concern>   
     - write concern. Default = w:1

To generate these options I first declared the options I wanted to use inside my `options.json <https://github.com/bryanreinero/Firehose/blob/master/src/main/java/options.json>`_ file. Here's a snippet of the file:

::

    {
    "application": "Firehose",
    "options": [
        {
            "op": "m",
            "longOpt" : "mongos",
            "name": "hostname:port",
            "description": "',' delimited list of mongodb host to connect to. Default localhost:27017,",
            "args": "multi",
            "separator": ","
        },
        {
            "op": "f",
            "longOpt" : "file",
            "name": "file",
            "description":"filename to import (full path)",
            "required": true,
            "args" : 1
        },
        {
            "op": "t",
            "longOpt" : "threads",
            "name": "threads",
            "description": "number of worker threads. Default 1",
            args: 1
        }
    } 

Firehose will read this file at application start up, creating the specific command line options I need to run the application. Now, all I need to do is define a set of callbacks which handle the processing of my command line options when a user actually runs the DSV Import tool. For example, here's the callback for handling input on the "-t" (or number of worker threads in the pool) option.

::

        cli.addCallBack("t", new CallBack() {
            @Override
            public void handle(String[] values) {
                numThreads = Integer.parseInt(values[0]);
            }
        });

You can examine more callback examples in the `code <https://github.com/bryanreinero/Firehose/blob/master/src/main/java/com/bryanreinero/util/Application.java#L94>`_. 

Example run
~~~~~~~~~~~

::

 java -jar target/Firehose-0.1.0.one-jar.jar -f test.csv -d , -ns test.firehose -h _id:objectid,count:float,sum:float,name:string -t 2

This command line invokes Firehose with 2 threads, parsing a CSV file of 4 columns. Each column is to be translated into json fields named "_id", "count", "sum" and "name", of types ObjectId, float, float, string respectively.

Using The Application Framework
-------------------------------

Firehose's application framework made for standing up simple load test quickly. As such, it comes with a set of command line options fully configured for control of the worker pool, instrumentation library and access to MongoDB. Users of the application framework need only to add:

    - Any extra command line options specific to their application
    - An instance of `Executable <https://github.com/bryanreinero/Firehose/blob/master/src/main/java/com/bryanreinero/util/WorkerPool.java#L9>`_ which the worker pool calls as a unit of work 


Let's again use the DSV import tool as an example. The application framework is initialize inside Firehose's `constructor <https://github.com/bryanreinero/Firehose/blob/master/src/main/java/com/bryanreinero/firehose/Firehose.java#L30>`_. The first step is to define the appropriate command line interface callbacks I need to handle user input.

::

        public Firehose ( String[] args ) throws Exception {
        
        Map<String, CallBack> myCallBacks = new HashMap<String, CallBack>();
        
        // custom command line callback for csv conversion
        myCallBacks.put("h", new CallBack() {
            @Override
            public void handle(String[] values) {
                for (String column : values) {
                    String[] s = column.split(":");
                    converter.addField( s[0], Transformer.getTransformer( s[1] ) );
                }
            }
        });
        
        // custom command line callback for delimeter
        myCallBacks.put("d", new CallBack() {
            @Override
            public void handle(String[] values) {
                converter.setDelimiter( values[0] );
            }
        });

        // custom command line callback for delimeter
        myCallBacks.put("f", new CallBack() {
            @Override
            public void handle(String[] values) {
                filename  = values[0];
                try { 
                    br = new BufferedReader(new FileReader(filename));
                }catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });

Remember, the `Application <https://github.com/bryanreinero/Firehose/blob/master/src/main/java/com/bryanreinero/util/Application.java#L92>`_ class has already defined CLI callbacks for the worker pool, instrumentation engine and MongoDB driver. All I needed to add where the callbacks for the input file, value delimiter and column headers. I've defined these callbacks as a collection of anonymous functions which I pass to the Application class' constructor:

::

    worker = Application.ApplicationFactory.getApplication(this, args, myCallBacks);

The Application class' constructor takes 3 parameters
    1. A class which implements Executor
    #. A String array of the command line options
    #. A list of custom command line callbacks

Bingo. I'm ready to rock and roll. Notice that the 'this' in the first parameter refers to an instance of the Firehose class, which implements Executable. The overridden `execute() <https://github.com/bryanreinero/Firehose/blob/master/src/main/java/com/bryanreinero/firehose/Firehose.java#L76>`_ method is where all the work is done. 

Build and Quickly Test Firehose
-------------------------------

I've included a CSV file generator called RandomDSVGenerator so that you may test your build and see Firehose in action with minimal effort. Simply run the following commands from the the command line prompt.

::
    $ mvn compile 
    $ java -cp target/Firehose-0.1.0.jar com.bryanreinero.firehose.test.RandomDSVGenerator so that you may-n 10000
    $ java -jar target/Firehose-0.1.0.one-jar.jar -f test.csv -d , -ns test.firehose -h _id:objectid,count.0:float,count.1:float,name:string -t 20

Why Firehose?
-------------

As a consultant, I often advise my clients to instrument their application code such that they have a baseline of performance metrics. Instrumenting Getting baselines are extremely useful both in identifying bottlenecks as well as understanding how much concurrency your application can handle, determine what latency is "normal" for the application and indicate when performance is deviating from those norms.

While most developers will acknowledge the value of instrumentation, few actually implement it. So to help them along, Firehose was designed with some basic instrumentation boiled right into it.

Dependencies
------------

Firehose is supported and somewhat tested on Java 1.7

Additional dependencies are:
    - `MongoDB Java Driver <http://docs.mongodb.org/ecosystem/drivers/java/>`_
    - `JUnit 4 <http://junit.org/>`_
    - `Apache Commons CLI 1.2 <http://commons.apache.org/proper/commons-cli/>`_

    
License
-------
Copyright (C) {2013}  {Bryan Reinero}

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.


Disclaimer
----------
This software is not supported by MongoDB, Inc. under any of their commercial support subscriptions or otherwise. Any usage of Firehose is at your own risk. Bug reports, feature requests and questions can be posted in the Issues section here on github.

To Do
-----
- Accept piped input from stdine
- Write Javadocs
- Accept json input
- Accept mongoexport formated csv's
- fix README formatting
