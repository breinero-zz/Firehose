========
Firehose
========

:Description: A tool import text files such as CSV's and TSV's into MongoDB.
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

Parameters
~~~~~~~~~~
-f file file name to import
-n database.collection  The target namespace in MongoDB to write to


Options
~~~~~~~
-b number   Queue size, the maximum number of documents queued for insertion to MongoDB. Defaults to 50000 items
-d []	The durability level used when writing to MongoDB. write concern.Default: NORMAL
-h	help print help info
-m	delimited list of mongos'es. Defaults to localhost:27017
-r	numthreads   number of threads used to read from input file. Defaults to 4
-w number   number of threads used to write into MongoDB. Defaults to 4


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

Accept piped input from stdin
Should create a FirehoseOptions class with an internal Builder class, rather than building the LoadClient in the CommandLineInterface
