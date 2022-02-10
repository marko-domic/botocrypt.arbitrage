package com.botocrypt.arbitrage.actor.receiver

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

object Receiver {

  sealed trait Info

  case class CoinInfo() extends Info

  def apply(): Behavior[Info] = Behaviors.setup {
    context => new Receiver(context).receive()
  }
}

class Receiver private(context: ActorContext[Receiver.Info]) {
  import Receiver._

  private def receive(): Behavior[Info] = Behaviors.receiveMessage {
    case coinInfo: CoinInfo =>
      Behaviors.same
  }
}
