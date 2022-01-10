package com.botocrypt.arbitrage.actor.init

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.botocrypt.arbitrage.actor.currency.Coin
import com.botocrypt.arbitrage.actor.currency.Coin.ConversionData

import scala.util.control.Breaks.{break, breakable}

object CoinInitializer {

  sealed trait Initializing

  case class CreateCoinNetwork() extends Initializing

  def apply(): Behavior[Initializing] = Behaviors.setup {
    context => new CoinInitializer(context, Map.empty).messageLoop()
  }
}

class CoinInitializer private(context: ActorContext[CoinInitializer.Initializing],
                              var coins: Map[String, ActorRef[Coin.CoinUpdate]]) {

  import CoinInitializer._

  private def messageLoop(): Behavior[Initializing] = initialize()

  private def initialize(): Behavior[Initializing] = Behaviors.receiveMessage {

    case createCoinNetwork: CreateCoinNetwork =>

      context.log.info("Start with initializing coin actors.")

      for ((exchange, coinPairsInfo) <- CoinInitValues.CoinsPerExchange) {
        for ((coinCurrencyId, pairInfo) <- coinPairsInfo) {
          breakable {

            // Generate coin identity
            val coinId = createCoinIdentity(coinCurrencyId, exchange)

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
            val coinActor = createCoin(coinCurrencyId, exchange, pairPrices, conversionDataSet)
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

      context.log.info("Coin actors initialized successfully.")

      doneInitializing()
  }

  private def doneInitializing(): Behavior[Initializing] = Behaviors.receiveMessage {
    _ => {
      context.log.info("Coin actors are already initialized.")
      Behaviors.same
    }
  }

  protected def createCoin(id: String, exchange: String, pairPrices: Map[String, Double],
                           exchangePairs: Set[ConversionData]): ActorRef[Coin.CoinUpdate] = {
    context.spawn(Coin(id, exchange, pairPrices, exchangePairs), createCoinIdentity(id, exchange))
  }

  private def createCoinIdentity(id: String, exchange: String): String = s"$exchange:$id"
}
