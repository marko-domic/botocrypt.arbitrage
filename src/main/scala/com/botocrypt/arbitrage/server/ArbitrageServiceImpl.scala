package com.botocrypt.arbitrage.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.botocrypt.arbitrage.api.{ArbitrageService, CoinPairDto, CoinPairResponseDto}

import scala.concurrent.Future

class ArbitrageServiceImpl extends ArbitrageService {

  override def sendCoinPairInfoFromExchange(
                                             in: Source[CoinPairDto, NotUsed]
                                           ): Future[CoinPairResponseDto] = ???
}
