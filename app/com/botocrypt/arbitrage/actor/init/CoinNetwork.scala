package com.botocrypt.arbitrage.actor.init

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import com.botocrypt.arbitrage.actor.currency.Coin
import com.botocrypt.arbitrage.actor.currency.Coin.ConversionData
import com.botocrypt.arbitrage.actor.notification.Informer
import com.botocrypt.arbitrage.constant.CoinInitValues
import com.botocrypt.arbitrage.util.CoinIdentity
import play.api.Logger

import scala.util.control.Breaks.{break, breakable}

object CoinNetwork {

  private val logger = Logger.apply(this.getClass)

  def initialize(context: ActorContext[NetworkInitializer.Initialize],
                 informer: ActorRef[Informer.OpportunityAlert],
                 existingCoins: Map[String, ActorRef[Coin.Update]]):
  Map[String, ActorRef[Coin.Update]] = {

    logger.info("Initializing coin network")

    var coins: Map[String, ActorRef[Coin.Update]] = Map.empty

    // Create coin actors with values from init values object
    for ((exchange, coinPairsInfo) <- CoinInitValues.CoinsPerExchange) {
      for ((coinCurrencyId, pairInfo) <- coinPairsInfo) {
        breakable {

          if (existingCoins.nonEmpty && coinCurrencyId == "ETH") {
            break()
          }

          // Generate coin identity
          val coinId = CoinIdentity.getCoinId(coinCurrencyId, exchange)

          // Validate if specific actor is already created. If so, continue with the loop
          if (coins.contains(coinId)) {
            break()
          }

          // Validate if specific actor is created in earlier initialization
          if (existingCoins.contains(coinId)) {
            coins += coinId -> existingCoins(coinId)
            break()
          }

          // Create new conversion pairs data and zero pair prices
          var pairPrices: Map[String, Double] = Map.empty
          var conversionDataMap: Map[String, ConversionData] = Map.empty
          for (info <- pairInfo) {
            val landingCoinBaseId = info.landingCurrencyId
            val landingExchange = info.exchange
            val landingCoinId = CoinIdentity.getCoinId(landingCoinBaseId, landingExchange)

            if (landingExchange == exchange) {
              pairPrices += landingCoinId -> 0.00
            }

            val conversionData = ConversionData(landingCoinBaseId, null, landingExchange,
              info.commissions)
            conversionDataMap += landingCoinId -> conversionData
          }

          // Create new coin actor and add it to final map
          val coinActor = createCoinActor(context, coinCurrencyId, exchange, pairPrices,
            conversionDataMap, informer)

          coins += coinId -> coinActor
        }
      }
    }

    // Update all coin actors with the rest of coin actors
    for ((coinId, coinActor) <- coins) {
      for ((landingCoinId, landingCoinActor) <- coins) {
        if (coinId != landingCoinId) {
          coinActor ! Coin.PairActorUpdate(landingCoinId, landingCoinActor)
        }
      }
    }

    logger.info("Coin network initialized successfully")

    coins
  }

  private def createCoinActor(context: ActorContext[NetworkInitializer.Initialize], coinBaseId: String,
                              exchange: String, pairPrices: Map[String, Double],
                              conversionDataMap: Map[String, ConversionData],
                              informer: ActorRef[Informer.OpportunityAlert]): ActorRef[Coin.Update] = {
    context.spawn(Coin.apply(coinBaseId, exchange, pairPrices, conversionDataMap, informer),
      CoinIdentity.getCoinId(coinBaseId, exchange))
  }
}
