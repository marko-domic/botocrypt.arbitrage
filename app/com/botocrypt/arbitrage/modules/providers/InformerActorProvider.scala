package com.botocrypt.arbitrage.modules.providers

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import com.botocrypt.arbitrage.actor.notification.Informer
import com.google.inject.Provider

import javax.inject.Inject

class InformerActorProvider @Inject()(actorSystem: ActorSystem)
  extends Provider[ActorRef[Informer.Update]] {
  override def get(): ActorRef[Informer.Update] = {
    actorSystem.spawn(Informer.apply(), "informer-actor")
  }
}
