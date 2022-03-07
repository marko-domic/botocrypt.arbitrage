package com.botocrypt.arbitrage.actor.receiver

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import com.botocrypt.arbitrage.actor.currency.Coin
import com.botocrypt.arbitrage.actor.init.NetworkInitializer
import org.scalatest.wordspec.AnyWordSpecLike

class ReceiverTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A Receiver" must {

    "initialize" in {

      // Create necessary actors and messages
      val networkInitializerProbe = testKit.createTestProbe[NetworkInitializer.Initialize]()
      val receiver = testKit.spawn(Receiver(networkInitializerProbe.ref), "receiver-test-1")
      val coinInfo: Receiver.CoinInfo = Receiver.CoinInfo("CEX.IO", "BTC", "USD", 40600.00, 50,
        40750.00, 50)

      // Send message which will trigger initialization
      receiver ! coinInfo

      // Validate message sent to network initializer actor
      networkInitializerProbe.expectMessageType[NetworkInitializer.CreateCoinNetwork]
    }

    "network initialized" in {

      // Create necessary actors and messages
      val networkInitializerProbe = testKit.createTestProbe[NetworkInitializer.Initialize]()
      val btcCoinProbe = testKit.createTestProbe[Coin.Update]()
      val usdCoinProbe = testKit.createTestProbe[Coin.Update]()
      val receiver = testKit.spawn(Receiver(networkInitializerProbe.ref), "receiver-test-2")
      val coins: Map[String, ActorRef[Coin.Update]] = Map(
        "CEX.IO:BTC" -> btcCoinProbe.ref,
        "CEX.IO:USD" -> usdCoinProbe.ref
      )
      val coinNetworkInitialized: Receiver.CoinNetworkInitialized =
        Receiver.CoinNetworkInitialized(coins)
      val coinInfo: Receiver.CoinInfo = Receiver.CoinInfo("CEX.IO", "BTC", "USD", 40600.00, 50,
        40750.00, 50)

      // Send message which will trigger initialization
      receiver ! coinInfo

      // Validate message sent to network initializer actor
      networkInitializerProbe.expectMessageType[NetworkInitializer.CreateCoinNetwork]

      // Send coin network initialized message
      receiver ! coinNetworkInitialized

      // Validate messages sent to coin actorsX
      btcCoinProbe.expectMessageType[Coin.PriceUpdate]
      usdCoinProbe.expectMessageType[Coin.PriceUpdate]
    }
  }

  "process coin info" in {

    // Create necessary actors and messages
    val networkInitializerProbe = testKit.createTestProbe[NetworkInitializer.Initialize]()
    val btcCoinProbe = testKit.createTestProbe[Coin.Update]()
    val usdCoinProbe = testKit.createTestProbe[Coin.Update]()
    val receiver = testKit.spawn(Receiver(networkInitializerProbe.ref), "receiver-test-3")
    val coins: Map[String, ActorRef[Coin.Update]] = Map(
      "CEX.IO:BTC" -> btcCoinProbe.ref,
      "CEX.IO:USD" -> usdCoinProbe.ref
    )
    val coinNetworkInitialized: Receiver.CoinNetworkInitialized =
      Receiver.CoinNetworkInitialized(coins)
    val coinInfoBeforeInit: Receiver.CoinInfo = Receiver.CoinInfo("CEX.IO", "BTC", "USD", 40600.00,
      50, 40750.00, 50)
    val coinInfoAfterInit: Receiver.CoinInfo = Receiver.CoinInfo("CEX.IO", "BTC", "USD", 40601.00,
      50, 40748.00, 50)

    // Send message which will trigger initialization
    receiver ! coinInfoBeforeInit

    // Validate message sent to network initializer actor
    networkInitializerProbe.expectMessageType[NetworkInitializer.CreateCoinNetwork]

    // Send coin network initialized message
    receiver ! coinNetworkInitialized

    // Validate messages sent to coin actors
    btcCoinProbe.expectMessageType[Coin.PriceUpdate]
    usdCoinProbe.expectMessageType[Coin.PriceUpdate]

    // Send coin prices update once again
    receiver ! coinInfoAfterInit

    // Network initializer should receive no messages
    networkInitializerProbe.expectNoMessage()

    // Validate messages sent to coin actors once again
    btcCoinProbe.expectMessageType[Coin.PriceUpdate]
    usdCoinProbe.expectMessageType[Coin.PriceUpdate]
  }
}
