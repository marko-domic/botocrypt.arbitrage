# Arbitrage Service

A part of the Botocrypt platform, the main purpose of this service is to process collected cryptocurrency exchanges 
data from aggregator and to find differences in prices. Using Akka toolkit, each coin for every exchange has up-to-date
prices and syncs with other coins. After finding trading path opportunity, subscribed users are notified about it.

## Architecture overview

An overview of Botocrypt architecture and Arbitrage service looks like this (using C4 model for visualising):

&nbsp;&nbsp;

*Level 1 - System context*
&nbsp;&nbsp;&nbsp;&nbsp;
![Level 1 - System context](http://www.plantuml.com/plantuml/proxy?cache=no&fmt=svg&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/level-1-system-context.wsd)

&nbsp;&nbsp;

*Level 2 - Container diagram*
&nbsp;
![Level 2 - Container diagram](http://www.plantuml.com/plantuml/proxy?cache=no&fmt=svg&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/level-2-container-diagram.wsd)

&nbsp;&nbsp;

*Level 3 - Component diagram*
&nbsp;
![Level 3 - Component diagram](http://www.plantuml.com/plantuml/proxy?cache=no&fmt=svg&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/level-3-component-diagram.wsd)

&nbsp;&nbsp;

There are 2 different role types of Arbitrage service. 

&nbsp;&nbsp;

*Initializing all necessary actors and calculating price difference between crypto coins*
&nbsp;
![Actor initialization sequence diagram](http://www.plantuml.com/plantuml/proxy?cache=no&fmt=svg&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/actor-init-and-finding-trading-path-sequence-diagram.wsd)

&nbsp;&nbsp;

*Adding new subscription for Botocrypt platform*
&nbsp;
![Arbitrage calculation sequence diagram](http://www.plantuml.com/plantuml/proxy?cache=no&fmt=svg&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/add-subscription-sequence-diagram.wsd)

## Registered exchanges

Exchanges from where Aggregator service receives cryptocurrency orders are:

* [CEX.IO](https://cex.io/)

## Registered currencies

Currencies with which Aggregator service works are:

* BTC
* USD
* ETH

## Technology Stack

## Technology Stack

### Server

| Technology                                                  | Description                                                                                            |
|-------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| <a href="https://www.scala-lang.org/download/">Scala</a>    | The Scala programming language                                                                         |
| <a href="https://www.playframework.com/">Play Framework</a> | Framework which follows the model–view–controller (MVC) architectural pattern                          |
| <a href="https://akka.io/">Akka</a>                         | Toolkit and runtime simplifying the construction of concurrent and distributed applications on the JVM |
| <a href="https://www.scala-sbt.org/">SBT</a>                | Build tool for Scala and Java projects                                                                 |

### Data

| Technology                                                                 | Description                                                       |
|----------------------------------------------------------------------------|-------------------------------------------------------------------|
| <a href="https://www.h2database.com/html/main.html">H2 Database Engine</a> | Java SQL database. Embedded and server modes; in-memory databases |

###  Libraries and Plugins

* [gRPC](https://grpc.io/) - Modern open source high performance Remote Procedure Call (RPC) framework that can run in 
any environment. It can efficiently connect services in and across data centers.
* [Mockito](https://site.mockito.org/) - Open source testing framework for Java which allows the creation of test double 
objects (mock objects) in automated unit tests for the purpose of test-driven development (TDD) or behavior-driven 
development (BDD).

### Other

* [git](https://git-scm.com/) - Free and Open-Source distributed version control system.
* [Docker](https://www.docker.com/) - A set of platform as a service products that use OS-level virtualization to 
deliver software in packages called containers.

## Installation, Build and Run

### Project download

Downloading Arbitrage service Play Framework project on local machine can be done by executing command:

```shell
git clone https://github.com/marko-domic/botocrypt.arbitrage.git
```

### Project build

Whole project was developed by [SBT build tool](https://www.scala-sbt.org/). Building and testing it is done with the 
help of the same tool, by executing command:

```shell
./sbt clean compile test
```

This command will trigger project build and all tests of the project, which are required steps in building phase.

### Build Docker image

### Running the application in production mode

```shell
./sbt stage
target/universal/stage/bin/arbitrage -Dplay.http.secret.key='BMKF7JN39bMOPOOuIqM7'
```

Secret key defined with parameter play.http.secret.key should be changed for every application starting in production 
mode

### Running the application locally

```shell
./sbt run
```

## License

GPL-3.0 © [Marko Domić](https://github.com/marko-domic)
