package com.botocrypt.arbitrage.routers

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.stream.scaladsl.Sink
import com.botocrypt.arbitrage.actor.receiver.Receiver
//import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.botocrypt.arbitrage.api.{AbstractArbitrageServiceRouter, CoinPairDto, CoinPairResponseDto}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ArbitrageRouter @Inject()(materializer: Materializer, system: ActorSystem, receiverActor: ActorRef[Receiver.Info])
  extends AbstractArbitrageServiceRouter(system) {

  override def sendCoinPairInfoFromExchange(in: Source[CoinPairDto, NotUsed]):
  Future[CoinPairResponseDto] = {
    implicit val mat = materializer
    implicit val ec: ExecutionContext = system.toTyped.executionContext
    in
      .map((coinPairDto: CoinPairDto) => {
        val status: Boolean = true

        // TODO: Implement logic for sending coinPairDto to coin actors

        CoinPairStreamResponseDto(coinPairDto.cycleId, status)
      })
      .reduce((lastStreamResponseDto: CoinPairStreamResponseDto, nextStreamResponseDto: CoinPairStreamResponseDto) => {
        val status: Boolean = true
        CoinPairStreamResponseDto(nextStreamResponseDto.cycleId, status)
      })
      .map((streamResponseDto: CoinPairStreamResponseDto) => CoinPairResponseDto(streamResponseDto.cycleId, (if (streamResponseDto.successfullyProcessed) "Success" else "Failed")))
      .runWith(Sink.last)
  }
}
