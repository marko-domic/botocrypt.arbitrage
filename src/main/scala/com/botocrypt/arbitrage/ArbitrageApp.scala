package com.botocrypt.arbitrage

import akka.actor.typed.ActorSystem
import com.botocrypt.arbitrage.actor.init.SystemInitializer
import com.typesafe.config.ConfigFactory

object ArbitrageApp extends App {

  // Important to enable HTTP/2 in ActorSystem's config
  val conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.defaultApplication())

  // Create System Initializer actor
  val systemInitializer: ActorSystem[SystemInitializer.CreateCoinNetwork] =
    ActorSystem(SystemInitializer(), "Arbitrage", conf)

  // Trigger initializing coin actors (creating network of them) and GRPC server
  systemInitializer ! SystemInitializer.CreateCoinNetwork()
}
