#Load Generator

##Control Database
The Load Generator Application get its configuration from a back end datastore called the control database. In addition, Load Generator records performance statistics into the control database.


##Example Workload
```
{
 id: "workloadName",
 operations: [
    <MongoOperaiton>,
    <MongoOperaiton>,
    ...
 ]
}
```

##MongoOperation
```
{
    "name": <String: Operation Name>,
    "datastores": [
     <String: A name that identifiegit s the target mongodb deployment>
     ]
    // this needs to be an array value so that the exact same workload can be duplicated on mulitple databases.
    // this facilitates A/B testing
    "namespace": <String: Names of target database and collection, Dot separated.>,
    "type": "mongodb", // this is redundant maybe
    "operation": [ insert | update | find | delete | command ],
    readPref: {},
    writeCon: {}
}
``` 

set a latency threshold per operation. if the mean latency is below 2 standard deviations from the mean, the server is 
capable of handling the current load. you should probably lower the target latency. the app should be able to determine
the right latency based on the standard deviation.