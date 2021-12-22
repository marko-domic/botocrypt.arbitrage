package com.botocrypt.arbitrage.actor.notification

import akka.actor.{Actor, ActorLogging, Props}

object Informer {

  case class OpportunityAlert()

  def props(): Props = Props(new Informer())
}

class Informer extends Actor with ActorLogging {

  override def receive: Receive = ???

}
