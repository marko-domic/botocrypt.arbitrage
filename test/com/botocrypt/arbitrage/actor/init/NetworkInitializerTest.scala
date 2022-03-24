package com.botocrypt.arbitrage.actor.init

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.botocrypt.arbitrage.actor.init.NetworkInitializer.CreateCoinNetwork
import com.botocrypt.arbitrage.actor.notification.Informer
import com.botocrypt.arbitrage.actor.receiver.Receiver
import org.scalatest.wordspec.AnyWordSpecLike

class NetworkInitializerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Network initializer" must {
    "initialize" in {

      // Create necessary actors
      val informerProbe = testKit.createTestProbe[Informer.Update]()
      val receiver = testKit.createTestProbe[Receiver.Info]()
      val initializer = testKit.spawn(NetworkInitializer(informerProbe.ref), "initializer-test-1")
      val createNetworkMessage = CreateCoinNetwork(Map.empty, receiver.ref)

      // Send network initialize message
      initializer ! createNetworkMessage

      // Validate if initializer replied to receiver actor
      receiver.expectMessageType[Receiver.CoinNetworkInitialized]
    }
  }
}
