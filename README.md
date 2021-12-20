# Arbitrage Service

A part of the botocrypt platform, the main purpose of this service is to process collected 
cryptocurrency exchanges data from aggregator and to find differences in prices.

## Architecture overview

An overview of Botocrypt architecture and Arbitrage service looks something like this (using C4 
model for visualising):

*Level 1 - System context*
![Level 1 - System context](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/level-1-system-context.wsd)

*Level 2 - Container diagram*
![Level 2 - Container diagram](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/level-2-container-diagram.wsd)

*Level 3 - Component diagram*
![Level 3 - Component diagram](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/marko-domic/botocrypt.arbitrage/main/doc/level-3-component-diagram.wsd)
