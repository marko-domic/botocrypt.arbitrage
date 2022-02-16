package com.botocrypt.arbitrage.actor.currency

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.botocrypt.arbitrage.actor.notification.Informer
import com.botocrypt.arbitrage.util.CoinIdentity
import play.api.Logger

object Coin {

  sealed trait Update

  case class PriceUpdate(landingCoinId: String, price: Double) extends Update

  case class PairActorUpdate(coinId: String, coinPairActor: ActorRef[Update])
    extends Update

  case class PoisonPill() extends Update

  case class ConversionData(landingCurrencyId: String, landingCurrency: ActorRef[Update],
                            exchange: String, commissions: (Double, Double))

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

  private def apply(): Behavior[Update] = Behaviors.receiveMessage {
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

      val newLandingConversionData = ConversionData(landingConversionData.landingCurrencyId,
        updateCoinActor, landingConversionData.exchange, landingConversionData.commissions)

      pairConversionData -= updateCoinId
      pairConversionData += updateCoinId -> newLandingConversionData
    }

    Behaviors.same
  }

  private def updateCoinPrice(priceUpdate: PriceUpdate): Behavior[Update] = {

    val landingCoinId = priceUpdate.landingCoinId
    if (pairPrices.contains(landingCoinId)) {
      pairPrices += landingCoinId -> priceUpdate.price
      // TODO: Implement sending request for opportunity search
    }

    Behaviors.same
  }
}
