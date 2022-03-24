package com.botocrypt.arbitrage.actor.currency

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.botocrypt.arbitrage.actor.notification.Informer
import com.botocrypt.arbitrage.util.CoinIdentity
import play.api.Logger

import scala.collection.immutable.ListMap
import scala.util.control.Breaks.{break, breakable}

object Coin {

  sealed trait Update

  case class PriceUpdate(landingCoinBaseId: String, price: Double, amount: Double) extends Update

  case class PairActorUpdate(coinId: String, coinPairActor: ActorRef[Update])
    extends Update

  case class Arbitrage(path: ListMap[String, ConvertedContext], exchangeToExchange: Boolean, fromCoin: ConvertedContext)
    extends Update

  case class PoisonPill() extends Update

  case class ConversionData(landingCoinBaseId: String, landingCoin: ActorRef[Update], exchange: String,
                            commissions: (Double, Double))

  case class ConvertedContext(coinBaseId: String, exchange: String, amount: Double)

  def apply(coinBaseId: String, exchange: String, pairPrices: Map[String, Double],
            pairConversionData: Map[String, Coin.ConversionData],
            informer: ActorRef[Informer.Update]): Behavior[Update] =
    Behaviors.setup {
      _ => new Coin(coinBaseId, exchange, pairPrices, pairConversionData, informer).apply()
    }
}

class Coin private(
                    coinBaseId: String,
                    exchange: String,
                    var pairPrices: Map[String, Double],
                    var pairConversionData: Map[String, Coin.ConversionData],
                    informer: ActorRef[Informer.Update]) {

  import Coin._

  private val logger: Logger = Logger(this.getClass)

  logger.info(s"$coinBaseId coin for exchange $exchange has been created.")

  protected def apply(): Behavior[Update] = Behaviors.receiveMessage {
    case coinPairActorUpdate: PairActorUpdate => setCoinPairActor(coinPairActorUpdate)
    case priceUpdate: PriceUpdate => updateCoinPrice(priceUpdate)
    case arbitrage: Arbitrage => findOpportunity(arbitrage)
    case _: PoisonPill => Behaviors.stopped
  }

  private def setCoinPairActor(coinPairActorUpdate: PairActorUpdate): Behavior[Update] = {

    logger.trace(s"SetCoinPairActor message received. Coin id: ${coinPairActorUpdate.coinId}")

    val updateCoinId = coinPairActorUpdate.coinId
    val updateCoinActor = coinPairActorUpdate.coinPairActor

    val landingConversionData: ConversionData = pairConversionData(updateCoinId)
    if (landingConversionData == null) {
      logger.warn(s"There is no conversion data for coin $updateCoinId.")
      return Behaviors.same
    }

    if (landingConversionData.landingCoin == null) {
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

    logger.trace(s"Coin price update message received. Landing coin: ${priceUpdate.landingCoinBaseId}, " +
      s"price: ${priceUpdate.price}, amount: ${priceUpdate.amount}")

    val landingCoinBaseId = priceUpdate.landingCoinBaseId
    if (pairPrices.contains(landingCoinBaseId)) {
      pairPrices += landingCoinBaseId -> priceUpdate.price

      val sourceAmount: Double = priceUpdate.amount
      val startingCoinId: String = CoinIdentity.getCoinId(coinBaseId, exchange)
      val statingContext: ConvertedContext = ConvertedContext(coinBaseId, exchange, sourceAmount)
      val startingPath: ListMap[String, ConvertedContext] = ListMap(startingCoinId -> statingContext)
      arbitrageInsideExchange(landingCoinBaseId, sourceAmount, startingPath)
    }

    Behaviors.same
  }

  private def findOpportunity(arbitrage: Arbitrage): Behavior[Update] = {

    logger.trace(s"Find arbitrage path message received. Current path: ${arbitrage.path}, " +
      s"from coin: ${arbitrage.fromCoin.coinBaseId}, from exchange: ${arbitrage.fromCoin.exchange}")

    val path: ListMap[String, ConvertedContext] = arbitrage.path
    val sourceAmount = path.last._2.amount
    val receivedFromExchange = arbitrage.exchangeToExchange
    val originCoinId = CoinIdentity.getCoinId(arbitrage.fromCoin.coinBaseId, arbitrage.fromCoin.exchange)

    // Search for opportunity inside the same exchange
    for ((landingCoinBaseId: String, _: Double) <- pairPrices) {
      breakable {
        val landingCoinId = CoinIdentity.getCoinId(landingCoinBaseId, exchange)

        // Check if landing coin is on path in the same exchange and not from previous coin
        if (path.contains(landingCoinId) && landingCoinId != originCoinId) {
          validateOpportunity(path(landingCoinId), sourceAmount, path)
          break()
        }

        // Check if landing coin is path but in other exchanges
        if (pathContainsCoin(path, landingCoinBaseId) && !receivedFromExchange) {
          validateOpportunity(findConvertedContextInPath(path, landingCoinBaseId), sourceAmount, path)
          break()
        }

        // If nothing is found, proceed with finding arbitrage path
        arbitrageInsideExchange(landingCoinBaseId, sourceAmount, path)
      }
    }

    // Proceed with arbitrage requests on other exchanges
    if (!receivedFromExchange) {
      arbitrageWithOtherExchanges(sourceAmount, path)
    }

    Behaviors.same
  }

  private def arbitrageInsideExchange(landingCoinBaseId: String, sourceAmount: Double,
                                      arbitragePath: ListMap[String, ConvertedContext]): Unit = {
    val landingCoinId: String = CoinIdentity.getCoinId(landingCoinBaseId, exchange)
    val landingContext: ConvertedContext = ConvertedContext(landingCoinBaseId, exchange,
      calculateLandingAmountInsideExchange(landingCoinId, landingCoinBaseId, sourceAmount))
    val landingPath: ListMap[String, ConvertedContext] = arbitragePath + (landingCoinId -> landingContext)
    pairConversionData(landingCoinId).landingCoin ! Arbitrage(landingPath, exchangeToExchange = false,
      ConvertedContext(coinBaseId, exchange, -1))
  }

  private def arbitrageWithOtherExchanges(sourceAmount: Double,
                                          arbitragePath: ListMap[String, ConvertedContext]): Unit = {

    for ((landingCoinId: String, conversionData: Coin.ConversionData) <- pairConversionData) {
      val landingCoinBaseId: String = conversionData.landingCoinBaseId
      val landingExchange: String = conversionData.exchange
      if (landingCoinBaseId == coinBaseId) {
        val landingAmount = calculateLandingAmountOnOtherExchange(landingCoinId, sourceAmount)
        val convertedContext: ConvertedContext = ConvertedContext(landingCoinBaseId, landingExchange, landingAmount)
        val landingPath: ListMap[String, ConvertedContext] = arbitragePath + (landingCoinId -> convertedContext)
        conversionData.landingCoin ! Arbitrage(landingPath, exchangeToExchange = true,
          ConvertedContext(coinBaseId, exchange, -1))
      }
    }
  }

  private def calculateLandingAmountInsideExchange(landingCoinId: String, landingCoinBaseId: String,
                                                   sourceAmount: Double): Double = {

    val landingPrice: Double = pairPrices(landingCoinBaseId)
    val landingAmount: Double = sourceAmount * landingPrice
    val commissions: (Double, Double) = pairConversionData(landingCoinId).commissions
    val calculatedCommission: Double = (landingAmount * (commissions._1 / 100)) + commissions._2

    landingAmount - calculatedCommission
  }

  private def calculateLandingAmountOnOtherExchange(landingCoinId: String, amount: Double): Double = {

    val commissions: (Double, Double) = pairConversionData(landingCoinId).commissions
    amount - ((amount * (commissions._1 / 100)) + commissions._2)
  }

  private def pathContainsCoin(arbitragePath: ListMap[String, ConvertedContext], landingCoinBaseId: String): Boolean = {
    findConvertedContextInPath(arbitragePath, landingCoinBaseId) != null
  }

  private def findConvertedContextInPath(arbitragePath: ListMap[String, ConvertedContext],
                                         landingCoinBaseId: String): ConvertedContext = {

    // Find converted context in arbitrage path for landing coin
    for ((_: String, landingConvertedContext: ConvertedContext) <- arbitragePath) {
      if (landingConvertedContext.coinBaseId == landingCoinBaseId) {
        return landingConvertedContext
      }
    }

    // Return null if context not found
    null
  }

  private def validateOpportunity(landingConvertedContext: ConvertedContext, sourceAmount: Double,
                                  path: ListMap[String, ConvertedContext]): Unit = {

    val landingCoinBaseId: String = landingConvertedContext.coinBaseId
    val landingCoinId: String = CoinIdentity.getCoinId(landingCoinBaseId, exchange)
    val newLandingAmount = calculateLandingAmountInsideExchange(landingCoinId, landingCoinBaseId, sourceAmount)

    // Check if new amount is greater then existing amount in arbitrage path
    if (newLandingAmount > landingConvertedContext.amount) {
      logger.info("Arbitrage opportunity found")
      informer ! convertPathToOpportunityAlert(path)
    }
  }

  private def convertPathToOpportunityAlert(path: ListMap[String, ConvertedContext]): Informer.Update = {

    val opportunityPath: List[Informer.CoinContext] = path.map {
      case (_: String, convertedContext: ConvertedContext) =>
        Informer.CoinContext(convertedContext.coinBaseId, convertedContext.exchange)
    }.toList

    Informer.OpportunityAlert(opportunityPath)
  }
}
