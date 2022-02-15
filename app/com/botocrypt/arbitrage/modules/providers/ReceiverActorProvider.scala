package com.botocrypt.arbitrage.modules.providers

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import com.botocrypt.arbitrage.actor.init.NetworkInitializer
import com.botocrypt.arbitrage.actor.receiver.Receiver
import com.google.inject.Provider

import javax.inject.Inject

class ReceiverActorProvider @Inject()(actorSystem: ActorSystem,
                                      networkInitializerActor:
                                      ActorRef[NetworkInitializer.Initialize])
  extends Provider[ActorRef[Receiver.Info]] {
  override def get(): ActorRef[Receiver.Info] = {
    actorSystem.spawn(Receiver.apply(networkInitializerActor), "receiver-actor")
  }
}
