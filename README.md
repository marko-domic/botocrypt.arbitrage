# Arbitrage Service

A part of the botocrypt platform, the main purpose of this service is to process collected 
cryptocurrency exchanges data from aggregator and to find differences in prices.

## Architecture overview

An overview of Botocrypt architecture and Arbitrage service looks something like this (using C4 
model for visualising):

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



