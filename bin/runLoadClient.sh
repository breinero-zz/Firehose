#! /bin/bash

# Java specific variables
JAVA=/usr/bin/java
JAVAOPTS="-Xmx5g"
CLASSPATH="-cp lib/mongo.jar:lib/commons-cli-1.2.jar:bin"
MAINCLASS="com.xgen.load.LoadClient"
BINDIR="-d bin/"
SRCDIR="-sourcepath src/"

# MongoDB specific variables

DURABILITY="-d acknowledged"
READERS="-r 2"
WRITERS="-w 4"
FILENAME="-f /Users/breinero/Desktop/prepped.csv"
#LOCATIONS=""
MONGOS="-m localhost:40000"
#NAMESPACE="-n.items"
#BUFFER="-b 25000"

if [ $1 == "compile" ] ; then 
	javac $CLASSPATH $BINDIR $SRCDIR src/com/xgen/load/*.java ; 
elif [ $1 == "help" ] ; then 
	 $JAVA $CLASSPATH $MAINCLASS -h
else
	$JAVA $JAVAOPTS $CLASSPATH $MAINCLASS $MONGOS $FILENAME $READERS $WRITERS 
fi
