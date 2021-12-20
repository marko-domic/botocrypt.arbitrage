package com.botocrypt.arbitrage.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.botocrypt.arbitrage.actor.Currency.ConversionData

object Currency {

  case class PriceUpdate(paymentCurrencyId: String, price: Double)

  case class ConversionData(landingCurrencyId: String, landingCurrency: ActorRef, exchange: String,
                             commissions: (Double, Double))

  def props(id: String, exchange: String, pairPrices: Map[String, Double],
            exchangePairs: Set[ConversionData]): Props =
    Props(new Currency(id, exchange, pairPrices, exchangePairs))
}

class Currency(id: String, exchange: String, pairPrices: Map[String, Double],
               exchangePairs: Set[ConversionData]) extends Actor with ActorLogging {

  import Currency._
  
  override def receive: Receive = ???

  private def getIdentity(): String = getIdentity(exchange, id)

  private def getIdentity(exchange: String, id: String): String = s"$exchange:$id"
}
