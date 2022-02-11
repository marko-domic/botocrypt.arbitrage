package com.botocrypt.arbitrage.routers

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.botocrypt.arbitrage.actor.receiver.Receiver
import com.botocrypt.arbitrage.api.{AbstractArbitrageServiceRouter, CoinPairDto, CoinPairOrderDto, CoinPairResponseDto}
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ArbitrageRouter @Inject()(materialize: Materializer, system: ActorSystem,
                                receiverActor: ActorRef[Receiver.Info])
  extends AbstractArbitrageServiceRouter(system) with Logging {

  override def sendCoinPairInfoFromExchange(in: Source[CoinPairDto, NotUsed]):
  Future[CoinPairResponseDto] = {
    implicit val mat: Materializer = materialize
    implicit val ec: ExecutionContext = system.toTyped.executionContext
    in
      .map((coinPairDto: CoinPairDto) => processCoinPairDto(coinPairDto))
      .reduce((lastResponse: CoinPairStreamResponseDto, nextResponse: CoinPairStreamResponseDto) => {
        CoinPairStreamResponseDto(nextResponse.cycleId,
          lastResponse.successfullyProcessed && nextResponse.successfullyProcessed)
      })
      .map((streamResponseDto: CoinPairStreamResponseDto) =>
        CoinPairResponseDto(streamResponseDto.cycleId,
          if (streamResponseDto.successfullyProcessed) "Success" else "Failed"))
      .runWith(Sink.last)
  }

  private def processCoinPairDto(coinPairDto: CoinPairDto): CoinPairStreamResponseDto = {
    try {

      logger.trace(s"CoinPairDto received for processing: ${coinPairDto}")

      // Send message to receiver actor
      receiverActor ! convertToCoinInfo(coinPairDto)

      CoinPairStreamResponseDto(coinPairDto.cycleId, successfullyProcessed = true)
    } catch {
      case e: Exception =>
        CoinPairStreamResponseDto(coinPairDto.cycleId, successfullyProcessed = false)
    }
  }

  private def convertToCoinInfo(coinPairDto: CoinPairDto): Receiver.CoinInfo = {
    val coinPairOrderDto: CoinPairOrderDto = coinPairDto.getCoinPairOrder
    Receiver.CoinInfo(
      coinPairOrderDto.exchange,
      coinPairOrderDto.firstCoin,
      coinPairOrderDto.secondCoin,
      coinPairOrderDto.bidAveragePrice,
      coinPairOrderDto.bidQuantity,
      coinPairOrderDto.askAveragePrice,
      coinPairOrderDto.askQuantity)
  }
}
