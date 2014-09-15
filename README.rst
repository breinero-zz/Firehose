========
Firehose
========

:Description: A tool import for text files such as CSV's and TSV's into MongoDB.
:Author: Bryan Reinero <breinero@gmail.com>

License
-------

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

Dependencies
============

Firehose is supported ands somewhat tested on Java 1.7

Additional dependencies are:
- MongoDB Java Driver `http://docs.mongodb.org/ecosystem/drivers/java/`
- (Unit Tests) JUnit 4  `http://junit.org/`
- Apache Commons CLI 1.2 `http://commons.apache.org/proper/commons-cli/`

Usage
-----

 -cr        print output in console mode, when possible
 -cols <columns>     ',' delimited list of columns [name:type]
 -delim              the value separator used to parse columns. Default
                     ','
 -dur <durability>   write concern. Default = NORMAL
 -f <file>           filename to import. REQUIRED
 -h                  print help info
 -i <interval>       sample interval over which to report stats,
                     (milleseconds)
 -m <mongos>         ',' delimited list of mongos'es. Default
                     localhost:27017,
 -n <namespace>      target namespace. REQUIRED
 -p <report>         print progress every 'n' seconds
 -t <threads>        number of threads. Default 1.
 -v                  Enable verbose output

Example run
-----------

::

 java -jar target/Firehose-0.1.0.one-jar.jar -f /Users/breinero/blah.csv  -n test.insert -cols _id:objectid,count:float,sum:float,name:string -t 2

This command line invokes Firehose with 2 threads, parsing a csv file of 4 columns. Each column is to be translated into json fields named "_id", "count", "sum" and "name", of types ObjectId, float, float, string respectively.

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

This output tells me that inserts are taking an average of 182 microseconds, as averaged over a time interval of 1000000 microsecs, (1 second). During this 1 second interval I inserted 7926 documents. As the output is printed in JSON format I can insert these stats into MongoDB for benchmarking analysis!

Using Firehose for Code Instrumentation
---------------------------------------

As a consultant, I often advise my clients to instrument their application code such that they have a baseline of performance metrics. Instrumenting Getting baselines are extremely useful both in identifying bottlenecks as well as understanding how much concurrency your application can handle, determine what latency is "normal" for the application and indicate when performance is deviating from those norms.


While most developers will acknowledge the value of instrumentation, few actually implement it. So to help them along, Firehose has some basic insturmentation boiled right into it. In fact, Firehose's csv import methods also serve as a great example of how you can use the instrumentation features in your own p.o.c.. Take a peek here:

https://github.com/bryanreinero/Firehose/blob/master/src/main/java/com/bryanreinero/firehose/Firehose.java#L35

Notice that the import of each record follows a three step process
- Read the next line from teh csv
- Parse the line and build a Java object ready for insertion
- Insert the new object into MongoDB

I want to know how much time each of these steps take, so I mark the start end of each step with use of the Interval.class. For instance, here's how I determine how long and individual insertion took

::

    Interval insert = samples.set("insert"); // Set the time marker
    dao.insert( object );
    insert.mark(); // mark the operation as complete
    
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
This software is not supported by MongoDB, Inc. under any of their commercial support subscriptions or otherwise. Any usage of mtools is at your own risk. Bug reports, feature requests and questions can be posted in the Issues section here on github.

To Do
-----
- Accept piped input from stdine
- Write Javadocs
- Accept json input
- Accept mongoexport formated csv's
- fix README formatting
