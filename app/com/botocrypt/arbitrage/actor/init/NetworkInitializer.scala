package com.botocrypt.arbitrage.actor.init

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.botocrypt.arbitrage.actor.currency.Coin
import com.botocrypt.arbitrage.actor.receiver.Receiver

object NetworkInitializer {

  sealed trait Initialize

  case class CreateCoinNetwork(context: ActorContext[Receiver.Info],
                               sender: ActorRef[Receiver.Info]) extends Initialize

  def apply(): Behavior[Initialize] = {
    Behaviors.receiveMessage[Initialize] {
      case createNetwork: CreateCoinNetwork =>

        // Initialize Arbitrage coin actor network
        val coins: Map[String, ActorRef[Coin.CoinUpdate]] =
          CoinNetwork.initialize(createNetwork.context)
        createNetwork.sender ! Receiver.CoinNetworkInitialized(coins)

        Behaviors.same
    }
  }
}
