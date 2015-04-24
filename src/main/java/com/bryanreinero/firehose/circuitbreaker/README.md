#Circuit Breaker

Software components often make calls to separate processes, often running on remote servers via a network. Service oriented architectures. Point of integration are the points in a distributed system when a process makes a call to a separate process. A distributed system's points of integration are often the places where failures occur and cascade. For example, the requesting client may end up waiting indefinitely for a response or simply handle a dropped connection poorly since the engineer fell prey to [The Common Fallacies of Distributed Computing](http://en.wikipedia.org/wiki/Fallacies_of_distributed_computing).


The Circuit breaker package can be configured to monitor for trigger conditions that trip the breaker automatically. This protects both the downstream server from overwhelming surges in load and the requesting client who has the circuit breaker. This is because once the circuit breaker has been tripped, the client application can respond appropriately. As opposed to simply hanging on its own pending requests to complete while the inbound load exhausts stack, heap, CPU or other resources.

##Components

###BreakerBox Class
Has a set of Circuit Breakers, and acts as a central queriable location for all the breakers being used. It's most likely that a given application, say a servlet container for instance, will only need one instance of a BreakerBox

###Circuit Breaker Class###
The circuit breaker has a set of thresholds which define the conditions when the circuit is to be broken (tripped) 

###Threshold Classes
There are currently three implementations of the threshold interface

- Latency
- OpsPerSecond
- ConcurrentOperations

Each of these immutable implementations of Threshold are initialized with a threshold value, which is used to trip the containing breaker when that value is exceeded.

####Trigger Conditions
- Latency Class `stats.getMean() >= max `
- OpsPerSecond Class `stats.getN() >= max`
- ConcurrentOperations Class `(stats.getN() * stats.getMean()) >= max` per [Little's law](http://en.wikipedia.org/wiki/Little%27s_law)

##JMX Integration 

An application's set of circuit breakers can be reset or have their thresholds changed via the `CircuitBreakerMBean` interface and the JMX server. 

###BreakerBox as JSON
```
{ 
    breakers: [ 
        { name: "foo", tripped: false, 
            thresholds: [ 
                { opsPerSec: 5000.0 }, 
                { latency: 1000.0 }, 
                { concurrency: 50.0 } 
            ] 
        }, 
        { name: "bar", tripped: false, 
            thresholds: [ 
                { latency: 1000.0 }, 
                { concurrency: 50.0 } 
            ] 
        }, 
        { name: "baz", tripped: false, 
            thresholds: [ 
                { latency: 1000.0 }, 
                { opsPerSec: 5000.0 }, 
                { concurrency: 50.0 } 
            ] 
        } 
    ] 
}
```


##Dependencies

This package uses the com.bryanreinero.firehose.metrics package to keep track of metrics, latencies and to calculate concurrency.
