#Retry 

##Strategies 
- Fail Fast
- Retry 

##Retry

###Retry Intervals
- Exponential Backoff 
- Constant


####Exponential Backoff
Function f(n) = 2^n - 1
where n = number of retries 

Abstract Class RetryStrategy 

#####Members
private static int limit
private AtomicInteger attempts
private static DateTime started
private static int retryInterval


#####Methods
DateTime getStartTime
int GetRetryInterval


class ExponentialBackoff implements RetryStrategy
class FailFast implements RetryStrategy
class RetryRegularly implements RetryStrategy

##Data Access Object
An abstraction 

##Has A
DataStoreOperation

##Operation

###Has Reliability Patterns
Time Out
Bounded Result Set
Circuit Breaker
Bulkhead
RetryStrategy
