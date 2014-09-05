========
Firehose
========

:Description: A tool import for text files such as CSV's and TSV's into MongoDB.
:Author: Bryan Reinero <breinero@gmail.com>

Dependencies
============

Firehose is supported ands somewhat tested on Java 1.7

Additional dependencies are:
- MongoDB Java Driver `http://docs.mongodb.org/ecosystem/drivers/java/`
- (Unit Tests) JUnit 4  `http://junit.org/`
- Apache Commons CLI 1.2 `http://commons.apache.org/proper/commons-cli/`

Usage
-----

 -c <console>        print output in console mode, when possible
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

Accept piped input from stdine

