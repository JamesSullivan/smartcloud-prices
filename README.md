# Smartcloud instance prices API

**Important: Do NOT fork this repository if you want to submit a solution.**

Imagine we run our infrastructure on a fictional cloud provider, Smartcloud. As their machine instance prices fluctuate all the time, Smartcloud provides an API for us to retrieve their prices in real time. This helps us in managing our cost.

# Requirements

Implement an API for fetching and returning machine instance prices from Smartcloud.

```
GET /prices?kind=sc2-micro
{"kind":"sc2-micro","amount":0.42}, ... (omitted)
```

This project scaffold provides an end-to-end implementation of an API endpoint which returns some dummy data. You should try to follow the same code structure.

You should implement `SmartcloudPriceService` to call the [smartcloud](https://hub.docker.com/r/smartpayco/smartcloud) endpoint and return the price data. Note that the smartcloud service has usage quota and may return error occassionally to simulate unexpected errors. Please make sure your service is able to handle the constraint and errors gracefully.

You should also include a README file to document:-
1. Any assumptions you make
1. Any design decisions you make
1. Instruction on how to run your code

You should use git and make small commits with meaningful commit messages as you implement your solution.

# Setup

Follow the instruction at [smartcloud](https://hub.docker.com/r/smartpayco/smartcloud) to run the Docker container on your machine.

Clone or download this project onto your machine and run

```
$ sbt run
```

The API should be running on your port 8080.

# How to submit

Please push your code to a public repository and submit the link via email. Please do not fork this repository.


# James Sullivan

## ASSUMPTIONS 

ASSUMPTION 1: GIVEN THIS IS A TEST WE WANT TO USE THE DEPENDENCIES AND VERSIONS IN THE ORIGINAL CODE. This necessitates use of previous LTS JDK as unmodified code throws an SBT error using the latest LTS JDK (specifically openjdk 21.0.2 2024-01-16).

```java.lang.ClassCastException: class java.lang.UnsupportedOperationException cannot be cast to class xsbti.FullReload (java.lang.UnsupportedOperationException is in module java.base of loader 'bootstrap'; xsbti.FullReload is in unnamed module of loader 'app')```

In actual production we might prefer the more secure latest LTS JDK and changes to dependencies to remove security issues such as the previous exception and this warning:

```WARNING: A terminally deprecated method in java.lang.System has been called WARNING: System::setSecurityManager has been called by sbt.TrapExit$ (file:/home/sullija/.sbt/boot/scala-2.12.14/org.scala-sbt/sbt/1.5.5/run_2.12-1.5.5.jar) WARNING: Please consider reporting this to the maintainers of sbt.TrapExit$ WARNING: System::setSecurityManager will be removed in a future release. ```

ASSUMPTION 2: THERE ARE NOT ANY RELEVANT INDUSTRY OR DEFACTO API STANDARDS SPECIFICALLY FOR CLOUD MACHINE INSTANCE PRICING THAT SHOULD BE FOLLOWED. The APIs used by AWS, Google Cloud, Azure, IBM, etc. all appear to be different, but we note that they mostly use REST APIs.

ASSUMPTION 3: GIVEN THE /get-instances QUERY THE LIST OF AVAILABLE INSTANCES IS DYNAMIC AND COULD CHANGE AT ANY TIME.  We cannot cache available instances nor can we hardcode checking of instance-type QueryParam before submitting to smartcloud endpoint. The prices are also considered to be very dynamic. If we knew otherwise we would consider caching for performance and usage reasons.


## DESIGN DECISIONS

DESIGN DECISION 1.0: GIVEN THE EXAMPLE "GET /prices?kind=sc2-micro" WE ARE LOOKING FOR AN EASILY UNDERSTANDABLE REST SOLUTION. Easily understandable from the perspective of both the implementor and user. There is no need for the complexity and verbosity of SOAP, nor an extremely performant solution (such as gRPC), nor a more flexible solution (such as GraphQL). A webhook API might be useful for real-time updating of price changes, but appears to be well beyond the scope of what is being asked for.

DESIGN DECISION 1.1: WE WILL USE JSON FOR RETURNING DATA. Given we have decided to use a REST API and the provided code JSON is the most natural format. We could easily add other options such as XML, but it is extra work and not clearly required. Application/JSON is automatically no-cache so we don't have to worry about setting no-cache headers to prevent stale data.

DESIGN DECISION 2: WE ARE NOT IMPLEMENTING AUTHENTICATION AND AUTHORIZATION. Although many APIs need these, there is nothing in the given project description indicating these are needed and we will not create unnecessary work. If prices changed based on user this would definitely be necessary.

DESIGN DECISION 3: UTILIZE HTTP FOR TEST SIMPLICITY. There is nothing in the requirements indicating sensitive data needing HTTPS (such as if the prices differed depending user). However, outside this job interview scenario we would use HTTPS as it is the default for the web now.

DESIGN DECISION 4: USE [HTTP Client](https://http4s.org/v1/docs/client.html) by adding "ember-client" dependency. We are already using http4s so this is only increasing our dependencies slightly. As the calls to smartcloud are idempotent we will also use [Retry middleware](https://http4s.org/v1/docs/client-middleware.html#retry) so that the client can recover from any issues.

DESIGN DECISION 5: SIGNATURE CHANGE Added InstanceKindService.Exception to InstanceKindService getAll() signature so that exceptions can be passed. Simple exception should suffice, no need for multiple messages or stack traces.

DESIGN DECISION 6 (Not implemented yet): WE WILL USE RFC 2616 STATUS CODES AND ADDITIONAL ERROR MESSAGES TO HELP THE CLIENT DETERMINE WHAT THE PROBLEM INVOLVES. 

Possible Codes
200 (OK)
400 (Bad Request)
404 (Not Found)
405 (Method Not Allowed)
406 (Not Acceptable)
409 (Conflict)
500 (Internal Server Error)

DESIGN DECISION 7 (started incomplete): TESTING SUITE

DESIGN DECISION 8 (not implemented yet): Consider Tracking smartcloud usage versus 1000 daily limit

## RUNNING CODE

### for production

```
sbt run
```
to test `curl --request GET 'http://localhost:8080/instance-kinds'`

### for dev (uses revolver plugin for auto recompiling on file change)

```
sbt 
~reStart
```

then to stop

```
<enter>
reStop
```

when already in SBT

```scalafmt```     Formatting code

```console```      REPL

```test```         Run test suite








