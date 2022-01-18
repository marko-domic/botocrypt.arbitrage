package com.botocrypt.arbitrage.actor.init

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.botocrypt.arbitrage.actor.currency.Coin
import com.botocrypt.arbitrage.server.ArbitrageServer

object SystemInitializer {

  sealed trait Initializing

  case class CreateCoinNetwork() extends Initializing

  def apply(): Behavior[Initializing] = Behaviors.setup {
    context => new SystemInitializer(context).messageLoop()
  }
}

class SystemInitializer private(context: ActorContext[SystemInitializer.Initializing]) {

  import SystemInitializer._

  private def messageLoop(): Behavior[Initializing] = initialize()

  private def initialize(): Behavior[Initializing] = Behaviors.receiveMessage {

    case _: CreateCoinNetwork =>

      // Initialize Arbitrage coin actor network
      var coins: Map[String, ActorRef[Coin.CoinUpdate]] = CoinNetwork.initialize(context)

      // Initialize Arbitrage gRPC server
      ArbitrageServer(context).run()

      context.log.info("Arbitrage system is up and running")

      doneInitializing()
  }

  private def doneInitializing(): Behavior[Initializing] = Behaviors.receiveMessage {
    _: Initializing => {
      context.log.info("Arbitrage system is already up and running")
      Behaviors.same
    }
  }
}
