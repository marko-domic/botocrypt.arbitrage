package com.botocrypt.arbitrage.actor.initializer

import akka.actor.{Actor, ActorLogging, Props}
import com.botocrypt.arbitrage.actor.initializer.CoinInitializer.CreateCoinNetwork

object CoinInitializer {

  case class CreateCoinNetwork()

  def props(): Props = Props(new CoinInitializer)
}

class CoinInitializer extends Actor with ActorLogging{

  override def receive: Receive = ready

  private def ready: Receive = {
    case createCoinNetwork: CreateCoinNetwork =>
      log.info("Start with initializing coin actors.")
      context.become(done)
  }

  private def done: Receive = {
    case _ => log.info("Coin actors are already initialized.")
  }
}
