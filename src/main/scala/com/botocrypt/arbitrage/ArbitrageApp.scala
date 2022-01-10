package com.botocrypt.arbitrage

import akka.actor.typed.ActorSystem
import com.botocrypt.arbitrage.actor.init.CoinInitializer

object ArbitrageApp extends App {

  //#actor-system
  val coinInitializer: ActorSystem[CoinInitializer.CreateCoinNetwork] =
    ActorSystem(CoinInitializer(), "Arbitrage")
  //#actor-system

  //#main-send-messages
  coinInitializer ! CoinInitializer.CreateCoinNetwork()
  //#main-send-messages

}
