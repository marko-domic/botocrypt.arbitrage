package com.botocrypt.arbitrage

import akka.actor.typed.ActorSystem
import com.botocrypt.arbitrage.actor.init.SystemInitializer
import com.typesafe.config.ConfigFactory

object ArbitrageApp extends App {

  // Create System Initializer actor
  val systemInitializer: ActorSystem[SystemInitializer.CreateCoinNetwork] =
    ActorSystem(SystemInitializer(), "Arbitrage")

  // Trigger initializing coin actors (creating network of them) and GRPC server
  systemInitializer ! SystemInitializer.CreateCoinNetwork()
}
