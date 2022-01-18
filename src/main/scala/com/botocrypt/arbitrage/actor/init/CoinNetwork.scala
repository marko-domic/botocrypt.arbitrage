package com.botocrypt.arbitrage.actor.init

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import com.botocrypt.arbitrage.actor.currency.Coin
import com.botocrypt.arbitrage.actor.currency.Coin.ConversionData
import com.botocrypt.arbitrage.constant.CoinInitValues
import com.botocrypt.arbitrage.util.CoinUtil

import scala.util.control.Breaks.{break, breakable}

object CoinNetwork {

  def initialize(context: ActorContext[SystemInitializer.Initializing]):
  Map[String, ActorRef[Coin.CoinUpdate]] = {

    context.log.info("Initializing coin actors")

    var coins: Map[String, ActorRef[Coin.CoinUpdate]] = Map.empty

    // Create coin actors with values from init values object
    for ((exchange, coinPairsInfo) <- CoinInitValues.CoinsPerExchange) {
      for ((coinCurrencyId, pairInfo) <- coinPairsInfo) {
        breakable {

          // Generate coin identity
          val coinId = CoinUtil.createCoinIdentity(coinCurrencyId, exchange)

          // Validate if specific actor is already created. If so, continue with the loop
          if (coins.contains(coinId)) {
            break
          }

          // Create new conversion pairs data and zero pair prices
          var pairPrices: Map[String, Double] = Map.empty
          var conversionDataSet: Set[ConversionData] = Set.empty
          for (info <- pairInfo) {
            val landingCoinBaseId = info.landingCurrencyId
            pairPrices += landingCoinBaseId -> 0.00
            val conversionData = ConversionData(landingCoinBaseId, null, info.exchange,
              info.commissions)
            conversionDataSet += conversionData
          }

          // Create new coin actor and add it to final map
          val coinActor = createCoin(context, coinCurrencyId, exchange, pairPrices,
            conversionDataSet)
          coins += coinId -> coinActor
        }
      }
    }

    // Update all coin actors with the rest of coin actors
    for ((coinId, coinActor) <- coins) {
      for ((landingCoinId, landingCoinActor) <- coins) {
        if (coinId != landingCoinId) {
          coinActor ! Coin.SetCoinPairActor(landingCoinId, landingCoinActor)
        }
      }
    }

    context.log.info("Coin actors initialized successfully")

    coins
  }

  private def createCoin(context: ActorContext[SystemInitializer.Initializing], id: String,
                         exchange: String, pairPrices: Map[String, Double],
                         exchangePairs: Set[ConversionData]): ActorRef[Coin.CoinUpdate] = {
    context.spawn(Coin(id, exchange, pairPrices, exchangePairs),
      CoinUtil.createCoinIdentity(id, exchange))
  }
}
