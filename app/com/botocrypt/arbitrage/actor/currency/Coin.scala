package com.botocrypt.arbitrage.actor.currency

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.botocrypt.arbitrage.actor.notification.Informer
import com.botocrypt.arbitrage.util.CoinIdentity
import play.api.Logger

import scala.collection.mutable

object Coin {

  sealed trait Update

  case class PriceUpdate(landingCoinBaseId: String, price: Double, quantity: Double) extends Update

  case class PairActorUpdate(coinId: String, coinPairActor: ActorRef[Update])
    extends Update

  case class Arbitrage(path: mutable.LinkedHashMap[String, ConvertedContext],
                       exchangeToExchange: Boolean) extends Update

  case class PoisonPill() extends Update

  case class ConversionData(landingCoinBaseId: String, landingCurrency: ActorRef[Update],
                            exchange: String, commissions: (Double, Double))

  case class ConvertedContext(coinBaseId: String, exchange: String, amount: Double)

  def apply(coinBaseId: String, exchange: String, pairPrices: Map[String, Double],
            pairConversionData: Map[String, Coin.ConversionData],
            informer: ActorRef[Informer.OpportunityAlert]): Behavior[Update] =
    Behaviors.setup {
      context =>
        new Coin(context, coinBaseId, exchange, pairPrices, pairConversionData, informer)
          .apply()
    }
}

class Coin private(context: ActorContext[Coin.Update],
                   coinBaseId: String,
                   exchange: String,
                   var pairPrices: Map[String, Double],
                   var pairConversionData: Map[String, Coin.ConversionData],
                   informer: ActorRef[Informer.OpportunityAlert]) {

  import Coin._

  private val logger: Logger = Logger(this.getClass)

  logger.info(s"$coinBaseId coin for exchange $exchange has been created.")

  protected def apply(): Behavior[Update] = Behaviors.receiveMessage {
    case coinPairActorUpdate: PairActorUpdate => setCoinPairActor(coinPairActorUpdate)
    case priceUpdate: PriceUpdate => updateCoinPrice(priceUpdate)
    case _: PoisonPill => Behaviors.stopped
  }

  private def setCoinPairActor(coinPairActorUpdate: PairActorUpdate): Behavior[Update] = {
    logger.trace("SetCoinPairActor message received.")

    val updateCoinId = coinPairActorUpdate.coinId
    val updateCoinActor = coinPairActorUpdate.coinPairActor

    val landingConversionData: ConversionData = pairConversionData(updateCoinId)
    if (landingConversionData == null) {
      logger.warn(s"There is no conversion data for coin $updateCoinId.")
      return Behaviors.same
    }

    if (landingConversionData.landingCurrency == null) {
      logger.info(s"Setting coin pair ${CoinIdentity.getCoinId(coinBaseId, exchange)} - " +
        s"$updateCoinId from exchange $exchange to exchange ${landingConversionData.exchange}.")

      val newLandingConversionData = ConversionData(landingConversionData.landingCoinBaseId,
        updateCoinActor, landingConversionData.exchange, landingConversionData.commissions)

      pairConversionData -= updateCoinId
      pairConversionData += updateCoinId -> newLandingConversionData
    }

    Behaviors.same
  }

  private def updateCoinPrice(priceUpdate: PriceUpdate): Behavior[Update] = {

    val landingCoinBaseId = priceUpdate.landingCoinBaseId
    if (pairPrices.contains(landingCoinBaseId)) {
      pairPrices += landingCoinBaseId -> priceUpdate.price

      val startingCoinId: String = CoinIdentity.getCoinId(coinBaseId, exchange)
      val statingContext: ConvertedContext = ConvertedContext(coinBaseId, exchange,
        priceUpdate.quantity)
      val startingPath: mutable.LinkedHashMap[String, ConvertedContext] =
        mutable.LinkedHashMap(startingCoinId -> statingContext)
      arbitrageInsideExchange(landingCoinBaseId, startingPath)
    }

    Behaviors.same
  }

  private def arbitrageInsideExchange(landingCoinBaseId: String,
                                      arbitragePath: mutable.LinkedHashMap[String, ConvertedContext]): Unit = {

    val landingCoinId: String = CoinIdentity.getCoinId(landingCoinId, exchange)
    val landingConversionData: ConversionData = pairConversionData(landingCoinId)
    val landingQuantity =
  }
}
