import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import com.botocrypt.arbitrage.actor.receiver.Receiver
import com.google.inject.{AbstractModule, TypeLiteral}

import javax.inject.{Inject, Provider}

class ReceiverActorProvider @Inject()(actorSystem: ActorSystem) extends Provider[ActorRef[Receiver.Info]] {
  override def get(): ActorRef[Receiver.Info] = {
    actorSystem.spawn[Receiver.Info](Receiver.apply(), "receiver-actor")
  }
}
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
class ActorModule extends AbstractModule {

  override def configure(): Unit = {
    bind(new TypeLiteral[ActorRef[Receiver.Info]]() {})
      .toProvider(classOf[ReceiverActorProvider])
      .asEagerSingleton()
  }
}
