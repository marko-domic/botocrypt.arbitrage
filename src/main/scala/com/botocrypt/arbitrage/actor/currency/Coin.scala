package com.botocrypt.arbitrage.actor.currency

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

object Coin {

  sealed trait CoinUpdate

  case class PriceUpdate(paymentCurrencyId: String, price: Double) extends CoinUpdate

  case class SetCoinPairActor(coinId: String, coinPairActor: ActorRef[CoinUpdate])
    extends CoinUpdate

  case class ConversionData(landingCurrencyId: String, landingCurrency: ActorRef[CoinUpdate],
                            exchange: String, commissions: (Double, Double))

  def apply(id: String, exchange: String, pairPrices: Map[String, Double],
            exchangePairs: Set[ConversionData]): Behavior[CoinUpdate] = Behaviors.setup {
    context => new Coin(context, id, exchange, pairPrices, exchangePairs).receive()
  }
}

class Coin private(context: ActorContext[Coin.CoinUpdate],
                   id: String,
                   exchange: String,
                   var pairPrices: Map[String, Double],
                   var exchangePairs: Set[Coin.ConversionData]) {
  import Coin._

  context.log.info(s"$id coin for exchange $exchange has been created.")

  private def receive(): Behavior[CoinUpdate] = Behaviors.receiveMessage {
    case setCoinPairActor: SetCoinPairActor =>

      context.log.debug("SetCoinPairActor message received.")

      val coinId = setCoinPairActor.coinId
      val coinActor = setCoinPairActor.coinPairActor

      for (conversionData <- exchangePairs) {
        val landingCoinBaseId = conversionData.landingCurrencyId
        val landingExchange = conversionData.exchange
        val landingCoinId = getIdentity(landingExchange, landingCoinBaseId)
        if (landingCoinId == coinId && conversionData.landingCurrency == null) {

          context.log.info(s"Setting coin pair $id:$landingCoinBaseId from exchange $exchange to exchange "
            + s"$landingExchange.")

          val landingCoinData = ConversionData(landingCoinBaseId, coinActor, landingExchange,
            conversionData.commissions)
          exchangePairs -= conversionData
          exchangePairs += landingCoinData
        }
      }

      Behaviors.same

    case priceUpdate: PriceUpdate =>

      // TODO: Implement logic for updating prices received from Botocrypt Aggregator

      Behaviors.same
  }

  private def getIdentity(): String = getIdentity(exchange, id)

  private def getIdentity(exchange: String, id: String): String = s"$exchange:$id"
}
