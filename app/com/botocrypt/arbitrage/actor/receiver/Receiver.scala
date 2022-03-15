package com.botocrypt.arbitrage.actor.receiver

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.botocrypt.arbitrage.actor.currency.Coin
import com.botocrypt.arbitrage.actor.init.NetworkInitializer
import com.botocrypt.arbitrage.util.CoinIdentity
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

  case class CoinNetworkInitialized(coins: Map[String, ActorRef[Coin.Update]]) extends Info

  def apply(networkInitializer: ActorRef[NetworkInitializer.Initialize]): Behavior[Info] = {
    Behaviors.setup {
      context => new Receiver(context, networkInitializer).apply()
    }
  }
}

class Receiver private(context: ActorContext[Receiver.Info],
                       systemInitializer: ActorRef[NetworkInitializer.Initialize]) {

  import Receiver._

  private val logger: Logger = Logger(this.getClass)
  private var coins: Map[String, ActorRef[Coin.Update]] = Map.empty

  protected def apply(): Behavior[Info] = initializeBehavior()

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
    systemInitializer ! NetworkInitializer.CreateCoinNetwork(coins, context.self)

    // Resend coin update message
    resendCoinInfoMessage(coinInfo)

    // After initializing coin actors, process coin info only
    networkInitializingBehavior()
  }

  private def resendCoinInfoMessage(coinInfo: CoinInfo): Behavior[Info] = {
    context.self ! coinInfo
    Behaviors.same
  }

  private def updateCoinsMap(coinsMap: Map[String, ActorRef[Coin.Update]]): Behavior[Info] = {

    // Remove coin actors which will not be used anymore
    for ((coinId, coinActor) <- coins) {
      // If coin actor is not in new generated coin map, destroy it by sending poison pill
      if (!coinsMap.contains(coinId)) {
        logger.info(s"Removing $coinId from network. Coin actor ref: $coinActor")
        coinActor ! Coin.PoisonPill()
      }
    }

    // Set new coin actors map
    coins = coinsMap

    processCoinInfoBehavior()
  }

  private def processCoinInfo(coinInfo: CoinInfo): Behavior[Info] = {

    logger.trace(s"$coinInfo received for processing")

    val exchange: String = coinInfo.exchange
    val firstCoinId: String = CoinIdentity.getCoinId(coinInfo.firstCoin, exchange)
    val secondCoinId: String = CoinIdentity.getCoinId(coinInfo.secondCoin, exchange)

    val firstCoinPrice: Double = coinInfo.askAveragePrice
    val secondCoinPrice: Double = 1 / coinInfo.bidAveragePrice

    val firstCoinQuantity: Double = coinInfo.askQuantity
    val secondCoinQuantity: Double = coinInfo.bidAveragePrice * coinInfo.bidQuantity

    coins(firstCoinId) ! Coin.PriceUpdate(coinInfo.secondCoin, firstCoinPrice, firstCoinQuantity)
    coins(secondCoinId) ! Coin.PriceUpdate(coinInfo.firstCoin, secondCoinPrice, secondCoinQuantity)

    Behaviors.same
  }
}
