# Arbitrage Service

A part of the botocrypt platform, the main purpose of this service is to process collected 
cryptocurrency exchanges data from aggregator and to find differences in prices.

## Architecture overview

An overview of Botocrypt architecture and Arbitrage service looks something like this (using C4 
model for visualising):

&nbsp;&nbsp;

*Level 1 - System context*
&nbsp;
![Level 1 - System context](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/level-1-system-context.wsd)

&nbsp;&nbsp;

*Level 2 - Container diagram*
&nbsp;
![Level 2 - Container diagram](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/level-2-container-diagram.wsd)

&nbsp;&nbsp;

*Level 3 - Component diagram*
&nbsp;
![Level 3 - Component diagram](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/level-3-component-diagram.wsd)

&nbsp;&nbsp;

There are 2 different role types of Arbitrage service. 

First is to initialize all necessary actors for price calculations, described on diagram bellow:

&nbsp;&nbsp;

*Actor initialization diagram*
&nbsp;
![Actor initialization sequence diagram](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/actor-init-sequence-diagram.wsd)

&nbsp;&nbsp;

Second and the main purpose of this service is to calculate price difference between crypto coins 
and exchanges as well:

&nbsp;&nbsp;

*Arbitrage calculation diagram*
&nbsp;
![Arbitrage calculation sequence diagram](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/arbitrage-calculation-sequence-diagram.wsd)



