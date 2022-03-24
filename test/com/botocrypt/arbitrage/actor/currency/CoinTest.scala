package com.botocrypt.arbitrage.actor.currency

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.botocrypt.arbitrage.actor.currency.Coin.PairActorUpdate
import com.botocrypt.arbitrage.actor.notification.Informer
import com.botocrypt.arbitrage.actor.notification.Informer.CoinContext
import org.scalatest.wordspec.AnyWordSpecLike

class CoinTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A Coin" must {
    "coin actor update" in {

      // Create input values, messages and actor probes
      val usdCoinProbe = testKit.createTestProbe[Coin.Update]()
      val actorUpdate = Coin.PairActorUpdate("CEX.IO:USD", usdCoinProbe.ref)
      val pairPrices = Map("USD" -> 40600.00)
      val usdData = Coin.ConversionData("USD", null, "CEX.IO", (0.00, 0.00))
      val pairConversionData = Map("CEX.IO:USD" -> usdData)
      val informerProbe = testKit.createTestProbe[Informer.Update]()
      val coin = testKit.spawn(Coin("BTC", "CEX.IO", pairPrices, pairConversionData,
        informerProbe.ref), "coin-test-1")

      // Send coin actor update message
      coin ! actorUpdate

      // Validate if no message has been sent
      usdCoinProbe.expectNoMessage()
      informerProbe.expectNoMessage()
    }

    "price update" in {

      // Create input values, messages and actor probes
      val usdCoinProbe = testKit.createTestProbe[Coin.Update]()
      val actorUpdate = Coin.PairActorUpdate("CEX.IO:USD", usdCoinProbe.ref)
      val priceUpdate = Coin.PriceUpdate("USD", 40700.00, 1)
      val pairPrices = Map("USD" -> 40600.00)
      val usdData = Coin.ConversionData("USD", null, "CEX.IO", (0.00, 0.00))
      val pairConversionData = Map("CEX.IO:USD" -> usdData)
      val informerProbe = testKit.createTestProbe[Informer.Update]()
      val coin = testKit.spawn(Coin("BTC", "CEX.IO", pairPrices, pairConversionData,
        informerProbe.ref), "coin-test-2")

      // Send coin actor update message
      coin ! actorUpdate

      // Send price update message
      coin ! priceUpdate

      // Validate if no message has been sent
      informerProbe.expectNoMessage()
    }

    "finding arbitrage opportunity" in {

      // Create informer actor probe
      val informerProbe = testKit.createTestProbe[Informer.Update]()

      // Values for BTC:CEX.IO actor
      val btcCexPairPrices = Map("USD" -> 40600.00)
      val usdCexData = Coin.ConversionData("USD", null, "CEX.IO", (0.25, 0.00))
      val btcBinanceExternalData = Coin.ConversionData("BTC", null, "Binance", (1.00, 0.00))
      val btcCexPairConversionData = Map("CEX.IO:USD" -> usdCexData, "Binance:BTC" -> btcBinanceExternalData)

      // Values for USD:CEX.IO actor
      val usdCexPairPrices = Map("BTC" -> 0.000024630541872)
      val btcCexData = Coin.ConversionData("BTC", null, "CEX.IO", (0.25, 0.00))
      val usdCexPairConversionData = Map("CEX.IO:BTC" -> btcCexData)

      // Values for BTC:Binance actor
      val btcBinancePairPrices = Map("USD" -> 41500.00)
      val usdBinanceData = Coin.ConversionData("USD", null, "Binance", (0.20, 0.00))
      val btcCexExternalData = Coin.ConversionData("BTC", null, "CEX.IO", (1.00, 0.00))
      val btcBinancePairConversionData = Map("Binance:USD" -> usdBinanceData, "CEX.IO:BTC" -> btcCexExternalData)

      // Values for USD:Binance actor
      val usdBinancePairPrices = Map("BTC" -> 0.000024449877751)
      val btcBinanceData = Coin.ConversionData("BTC", null, "Binance", (0.20, 0.00))
      val usdBinancePairConversionData = Map("Binance:BTC" -> btcBinanceData)

      // Create BTC CEX.IO actor
      val btcCexCoin = testKit.spawn(Coin("BTC", "CEX.IO", btcCexPairPrices, btcCexPairConversionData,
        informerProbe.ref), "btc-cex-coin-test-3")

      // Create USD CEX.IO actor
      val usdCexCoin = testKit.spawn(Coin("USD", "CEX.IO", usdCexPairPrices, usdCexPairConversionData,
        informerProbe.ref), "usd-cex-coin-test-3")

      // Create BTC Binance actor
      val btcBinanceCoin = testKit.spawn(Coin("BTC", "Binance", btcBinancePairPrices, btcBinancePairConversionData,
        informerProbe.ref), "btc-binance-coin-test-3")

      // Create USD CEX.IO actor
      val usdBinanceCoin = testKit.spawn(Coin("USD", "Binance", usdBinancePairPrices, usdBinancePairConversionData,
        informerProbe.ref), "usd-binance-coin-test-3")

      // Update coin actors with other coin actors
      btcCexCoin ! PairActorUpdate("CEX.IO:USD", usdCexCoin)
      btcCexCoin ! PairActorUpdate("Binance:BTC", btcBinanceCoin)

      usdCexCoin ! PairActorUpdate("CEX.IO:BTC", btcCexCoin)

      btcBinanceCoin ! PairActorUpdate("Binance:USD", usdBinanceCoin)
      btcBinanceCoin ! PairActorUpdate("CEX.IO:BTC", btcCexCoin)

      usdBinanceCoin ! PairActorUpdate("Binance:BTC", btcBinanceCoin)

      // Send price update message
      val priceUpdate = Coin.PriceUpdate("BTC", 0.000024600246002, 6501.561)
      usdCexCoin ! priceUpdate

      // Validate message sent to informer about arbitrage opportunity
      val opportunityPath: List[CoinContext] = List(CoinContext("USD", "CEX.IO"), CoinContext("BTC", "CEX.IO"),
        CoinContext("BTC", "Binance"), CoinContext("USD", "Binance"))
      informerProbe.expectMessage(Update(opportunityPath))
    }

    "arbitrage opportunity not found" in {

      // Create informer actor probe
      val informerProbe = testKit.createTestProbe[Informer.Update]()

      // Values for BTC:CEX.IO actor
      val btcCexPairPrices = Map("USD" -> 40600.00)
      val usdCexData = Coin.ConversionData("USD", null, "CEX.IO", (0.25, 0.00))
      val btcBinanceExternalData = Coin.ConversionData("BTC", null, "Binance", (1.00, 0.00))
      val btcCexPairConversionData = Map("CEX.IO:USD" -> usdCexData, "Binance:BTC" -> btcBinanceExternalData)

      // Values for USD:CEX.IO actor
      val usdCexPairPrices = Map("BTC" -> 0.000024630541872)
      val btcCexData = Coin.ConversionData("BTC", null, "CEX.IO", (0.25, 0.00))
      val usdCexPairConversionData = Map("CEX.IO:BTC" -> btcCexData)

      // Values for BTC:Binance actor
      val btcBinancePairPrices = Map("USD" -> 41200.00)
      val usdBinanceData = Coin.ConversionData("USD", null, "Binance", (0.20, 0.00))
      val btcCexExternalData = Coin.ConversionData("BTC", null, "CEX.IO", (1.00, 0.00))
      val btcBinancePairConversionData = Map("Binance:USD" -> usdBinanceData, "CEX.IO:BTC" -> btcCexExternalData)

      // Values for USD:Binance actor
      val usdBinancePairPrices = Map("BTC" -> 0.000024449877751)
      val btcBinanceData = Coin.ConversionData("BTC", null, "Binance", (0.20, 0.00))
      val usdBinancePairConversionData = Map("Binance:BTC" -> btcBinanceData)

      // Create BTC CEX.IO actor
      val btcCexCoin = testKit.spawn(Coin("BTC", "CEX.IO", btcCexPairPrices, btcCexPairConversionData,
        informerProbe.ref), "btc-cex-coin-test-4")

      // Create USD CEX.IO actor
      val usdCexCoin = testKit.spawn(Coin("USD", "CEX.IO", usdCexPairPrices, usdCexPairConversionData,
        informerProbe.ref), "usd-cex-coin-test-4")

      // Create BTC Binance actor
      val btcBinanceCoin = testKit.spawn(Coin("BTC", "Binance", btcBinancePairPrices, btcBinancePairConversionData,
        informerProbe.ref), "btc-binance-coin-test-4")

      // Create USD CEX.IO actor
      val usdBinanceCoin = testKit.spawn(Coin("USD", "Binance", usdBinancePairPrices, usdBinancePairConversionData,
        informerProbe.ref), "usd-binance-coin-test-4")

      // Update coin actors with other coin actors
      btcCexCoin ! PairActorUpdate("CEX.IO:USD", usdCexCoin)
      btcCexCoin ! PairActorUpdate("Binance:BTC", btcBinanceCoin)

      usdCexCoin ! PairActorUpdate("CEX.IO:BTC", btcCexCoin)

      btcBinanceCoin ! PairActorUpdate("Binance:USD", usdBinanceCoin)
      btcBinanceCoin ! PairActorUpdate("CEX.IO:BTC", btcCexCoin)

      usdBinanceCoin ! PairActorUpdate("Binance:BTC", btcBinanceCoin)

      // Send price update message
      val priceUpdate = Coin.PriceUpdate("BTC", 0.000024600246002, 6501.561)
      usdCexCoin ! priceUpdate

      // Validate that no message has been sent to informer
      informerProbe.expectNoMessage()
    }
  }
}
