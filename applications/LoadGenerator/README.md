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

### MongoOperation Schema Descriptor
```
{
    fields: [
        { name: "name", value;: [ { type: "string", prob: 1.0  } ] },
        { 
            name: "datastores", 
            value: [ 
                { 
                    type: "array", 
                    size: { min: 0 }, 
                    prob: 1.0,
                    values: [
                        { type: "string", prob: 1.0 } 
                    ]
                } 
            ]
        },
        { 
            name: "namespace",
            values: [ { type: "string", prob: 1.0 } ]
        },
        
    ]
}
```

##Document Descriptor
```
{
 fields: [
  { name: "a", values: [ { type: "int", min: 0, max: 10, probability: 1.0 } ] },
  { name: "b", values: [ { type: "int", min: 0, max: 10, probability: 1.0 } ] },
  { name: "c", values: [ { type: "int", min: 0, max: 10, probability: 1.0 } ] }
 ]
}
```

set a latency threshold per operation. if the mean latency is below 2 standard deviations from the mean, the server is 
capable of handling the current load. you should probably lower the target latency. the app should be able to determine
the right latency based on the standard deviation.