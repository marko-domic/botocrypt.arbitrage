package com.botocrypt.arbitrage.actor.currency

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.botocrypt.arbitrage.actor.currency.Coin.ConversionData

object Coin {

  case class PriceUpdate(paymentCurrencyId: String, price: Double)

  case class ConversionData(landingCurrencyId: String, landingCurrency: ActorRef, exchange: String,
                            commissions: (Double, Double))

  def props(id: String, exchange: String, pairPrices: Map[String, Double],
            exchangePairs: Set[ConversionData]): Props =
    Props(new Coin(id, exchange, pairPrices, exchangePairs))
}

class Coin(id: String, exchange: String, pairPrices: Map[String, Double],
           exchangePairs: Set[ConversionData]) extends Actor with ActorLogging {

  override def receive: Receive = ???

  private def getIdentity(): String = getIdentity(exchange, id)

  private def getIdentity(exchange: String, id: String): String = s"$exchange:$id"
}
