#Firehose DAO Package Design

Should the Firehose use dependency injection to load the DAO with a circuit-breaker, or other rate limiter? It makes sense to me as on the hub is initialized the user can then just send requests by name. Why might this dependency injection not be appropriate? It seems straight forward that the DataAccessHub should have this logic. So thinking in terms of application design and a classs hierarchy , how should the classes fit?

##Metrics
Keeps rate measurements and number of failures on a per operation basis. There is a need to report metrics at different hierarchical layers, i.e. operation, collection, database and cluster 

##Hub
Has a Map of DataAccessObjects 

##DataAccessObject
- Knows how to map objects to the database operations
- Has a retry / timeout strategy per operation 
- Has a circuit breaker per operation 

DAO interface has an execute method with one parameter, the DAORequestObject and returns a DAOResultObject

##DAORequestObject
- Subclassed per database API
- Request can be of operation types [ read | write | update | delete ]
- The cluster namespace identifier from which the DataAccessHub and DAO identify and target the right datastore instance.
- The DAORequestObject and the DAO itself are going to be closely related. 

Why? Because either the DAO or the DAORequestObject needs to know how to enforce schema validation. Consider that the Firehose application gets and instance of the DAORequestObject from the DAO itself.

##DAOResultObject
A standardized object that the Firehose application uses to determine if the request was successful or not.
- Database agnostic error reporting on timeouts and retry failures. 

##Closing in on a design that seems workable

###Interfaces
####AccessObject
#####Methods 
- execute
####Request
#####Methods
- ???
####Result
#####Methods
- isOK() returns boolean //ask Jeff why they removed this from the driver API
- error returns error code which can be used by the application client in a switch or 'or' clause


###Codec

####Datastore

```
{
    _id: <String>,
    type: <String["mongodb"]>
    uri: <URI>,
}
```
####OperationDescriptor

```
{
    name: "String",
    retryPolicy: {},
    operation: {}
}
```

####RetryPolicy

```
{
    maxRetries: <Intger>,
    maxDuration: <IntegerMS>,
    period: <IntegerMS>,
    type: <String[regularInterval|ExponentialBackoff]>
}
```

####MongoDAO

Extends schema from OperationDescriptor

```
{
    type: <String[insert | update | query | delete | command]>,
    namespace: <String[database.collection]>,
    query: {},
    projection: {},
    update; {},
    readPref: {},
    writeConcert: {}
}
```