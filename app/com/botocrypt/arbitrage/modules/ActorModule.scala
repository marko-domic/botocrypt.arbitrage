package com.botocrypt.arbitrage.modules

import akka.actor.typed.ActorRef
import com.botocrypt.arbitrage.actor.init.NetworkInitializer
import com.botocrypt.arbitrage.actor.notification.Informer
import com.botocrypt.arbitrage.actor.receiver.Receiver
import com.botocrypt.arbitrage.modules.providers.{NetworkInitializerActorProvider, ReceiverActorProvider}
import com.google.inject.{AbstractModule, TypeLiteral}
import play.api.libs.concurrent.AkkaGuiceSupport

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {

    bindTypedActor(Informer.apply(), "informer-actor")
    bind(new TypeLiteral[ActorRef[NetworkInitializer.Initialize]]() {})
      .toProvider(classOf[NetworkInitializerActorProvider])
      .asEagerSingleton()
    bind(new TypeLiteral[ActorRef[Receiver.Info]]() {})
      .toProvider(classOf[ReceiverActorProvider])
      .asEagerSingleton()
  }
}
