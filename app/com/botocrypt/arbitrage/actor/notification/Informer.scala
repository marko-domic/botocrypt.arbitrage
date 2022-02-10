package com.botocrypt.arbitrage.actor.notification

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Informer {

  case class OpportunityAlert()

  def apply(): Behavior[OpportunityAlert] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage { alert: OpportunityAlert =>

        // TODO: Implement logic for sending an alert

        Behaviors.same
      }
    }
}
