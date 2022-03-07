package com.botocrypt.arbitrage.actor.currency

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.botocrypt.arbitrage.actor.notification.Informer
import org.scalatest.wordspec.AnyWordSpecLike

class CoinTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A Coin" must {
    "coin actor update" in {

      // Create input values, messages and actor probes
      val usdCoinProbe = testKit.createTestProbe[Coin.Update]()
      val actorUpdate = Coin.PairActorUpdate("USD:CEX.IO", usdCoinProbe.ref)
      val pairPrices = Map("USD:CEX.IO" -> 40600.00)
      val usdData = Coin.ConversionData("USD", null, "CEX.IO", (0.00, 0.00))
      val pairConversionData = Map("USD:CEX.IO" -> usdData)
      val informerProbe = testKit.createTestProbe[Informer.OpportunityAlert]()
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
      val priceUpdate = Coin.PriceUpdate("USD:CEX.IO", 40700.00)
      val pairPrices = Map("USD:CEX.IO" -> 40600.00)
      val usdData = Coin.ConversionData("USD", null, "CEX.IO", (0.00, 0.00))
      val pairConversionData = Map("USD:CEX.IO" -> usdData)
      val informerProbe = testKit.createTestProbe[Informer.OpportunityAlert]()
      val coin = testKit.spawn(Coin("BTC", "CEX.IO", pairPrices, pairConversionData,
        informerProbe.ref), "coin-test-2")

      // Send price update message
      coin ! priceUpdate

      // Validate if no message has been sent
      informerProbe.expectNoMessage()
    }
  }
}
