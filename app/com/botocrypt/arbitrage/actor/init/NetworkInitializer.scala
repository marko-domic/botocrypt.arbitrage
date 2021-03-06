package com.botocrypt.arbitrage.actor.init

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.botocrypt.arbitrage.actor.currency.Coin
import com.botocrypt.arbitrage.actor.notification.Informer
import com.botocrypt.arbitrage.actor.receiver.Receiver
import com.botocrypt.arbitrage.constant.DestinationValues

object NetworkInitializer {

  sealed trait Initialize

  case class CreateCoinNetwork(existingCoins: Map[String, ActorRef[Coin.Update]],
                               sender: ActorRef[Receiver.Info]) extends Initialize

  def apply(informer: ActorRef[Informer.Update]): Behavior[Initialize] = {
    Behaviors.setup {
      context => new NetworkInitializer(context, informer).apply()
    }
  }
}

class NetworkInitializer private(context: ActorContext[NetworkInitializer.Initialize],
                                 informer: ActorRef[Informer.Update]) {

  import NetworkInitializer._

  protected def apply(): Behavior[Initialize] = networkInitializeBehavior()

  protected def networkInitializeBehavior(): Behavior[Initialize] = Behaviors.receiveMessage {
    case createNetwork: CreateCoinNetwork =>

      // Initialize Arbitrage coin actor network
      val coins: Map[String, ActorRef[Coin.Update]] =
        CoinNetwork.initialize(context, informer, createNetwork.existingCoins)

      // Initialize informer with default emails
      for (email <- DestinationValues.emails) {
        informer ! Informer.AddSubscription(email)
      }

      createNetwork.sender ! Receiver.CoinNetworkInitialized(coins)

      Behaviors.same
  }
}
