package com.botocrypt.arbitrage.actor.receiver

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.botocrypt.arbitrage.actor.currency.Coin
import com.botocrypt.arbitrage.actor.init.NetworkInitializer
import play.api.Logger

object Receiver {

  sealed trait Info

  case class CoinInfo(
                       exchange: String,
                       firstCoin: String,
                       secondCoin: String,
                       bidAveragePrice: Double,
                       bidQuantity: Double,
                       askAveragePrice: Double,
                       askQuantity: Double
                     ) extends Info

  case class CoinNetworkInitialized(coins: Map[String, ActorRef[Coin.CoinUpdate]]) extends Info

  def apply(systemInitializer: ActorRef[NetworkInitializer.Initialize]): Behavior[Info] =
    Behaviors.setup {
      context => new Receiver(context, systemInitializer).receive()
    }
}

class Receiver private(context: ActorContext[Receiver.Info],
                       systemInitializer: ActorRef[NetworkInitializer.Initialize]) {

  import Receiver._

  private val logger: Logger = Logger(this.getClass)
  private var coins: Map[String, ActorRef[Coin.CoinUpdate]] = Map.empty

  protected def receive(): Behavior[Info] = initializeBehavior()

  protected def initializeBehavior(): Behavior[Info] = Behaviors.receiveMessage {
    case coinInfo: CoinInfo => handleCoinInfoWithoutNetwork(coinInfo)
    case networkInitialized: CoinNetworkInitialized => updateCoinsMap(networkInitialized.coins)
  }

  protected def networkInitializingBehavior(): Behavior[Info] = Behaviors.receiveMessage {
    case coinInfo: CoinInfo => resendCoinInfoMessage(coinInfo)
    case networkInitialized: CoinNetworkInitialized => updateCoinsMap(networkInitialized.coins)
  }

  protected def processCoinInfoBehavior(): Behavior[Info] = Behaviors.receiveMessage {
    case coinInfo: CoinInfo => processCoinInfo(coinInfo)
    case networkInitialized: CoinNetworkInitialized => updateCoinsMap(networkInitialized.coins)
  }

  private def handleCoinInfoWithoutNetwork(coinInfo: CoinInfo): Behavior[Info] = {

    logger.info("Coin network initializing started.")

    // Trigger initializing coin actors (creating network of them)
    systemInitializer ! NetworkInitializer.CreateCoinNetwork(context, context.self)

    // Resend coin update message
    resendCoinInfoMessage(coinInfo)

    // After initializing coin actors, process coin info only
    networkInitializingBehavior()
  }

  private def resendCoinInfoMessage(coinInfo: CoinInfo): Behavior[Info] = {
    context.self ! coinInfo
    Behaviors.same
  }

  private def updateCoinsMap(coinsMap: Map[String, ActorRef[Coin.CoinUpdate]]): Behavior[Info] = {

    // Add new and remove already existing coin actors
    for ((coinId, coinActor) <- coinsMap) {
      if (coins.contains(coinId)) {
        // If coin actor already exists and it is not the same as created one, destroy it
        if (coinActor.path.toString != coins(coinId).path.toString) {
          coinActor ! Coin.PoisonPill()
        }
      } else {
        // Add new coin actor
        coins += coinId -> coinActor
      }
    }

    // Remove coin actors which are not necessary anymore
    for ((coinId, coinActor) <- coins) {
      if (!coinsMap.contains(coinId)) {
        coinActor ! Coin.PoisonPill()
      }
    }

    processCoinInfoBehavior()
  }

  private def processCoinInfo(coinInfo: CoinInfo): Behavior[Info] = {

    // TODO: Implement logic for sending updates to coin actors

    Behaviors.same
  }
}
