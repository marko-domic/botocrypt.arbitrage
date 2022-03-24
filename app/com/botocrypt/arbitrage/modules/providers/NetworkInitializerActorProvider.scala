package com.botocrypt.arbitrage.modules.providers

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import com.botocrypt.arbitrage.actor.init.NetworkInitializer
import com.botocrypt.arbitrage.actor.notification.Informer
import com.google.inject.Provider

import javax.inject.Inject

class NetworkInitializerActorProvider @Inject()(actorSystem: ActorSystem,
                                                informer: ActorRef[Informer.Update])
  extends Provider[ActorRef[NetworkInitializer.Initialize]] {
  override def get(): ActorRef[NetworkInitializer.Initialize] = {
    actorSystem.spawn(NetworkInitializer.apply(informer), "network-initializer-actor")
  }
}
