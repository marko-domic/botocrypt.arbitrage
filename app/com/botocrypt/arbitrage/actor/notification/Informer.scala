package com.botocrypt.arbitrage.actor.notification

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.botocrypt.arbitrage.service.MailerService
import play.api.Logger

object Informer {

  sealed trait Update

  case class AddSubscription(email: String) extends Update

  case class OpportunityAlert(path: List[CoinContext]) extends Update

  case class CoinContext(coinBaseId: String, exchange: String)

  def apply(mailerService: MailerService): Behavior[Update] =
    Behaviors.setup {
      _ => new Informer(mailerService, Set()).apply()
    }
}

class Informer private(val mailerService: MailerService, var subscriptions: Set[String]) {

  import Informer._

  private val logger: Logger = Logger(this.getClass)

  protected def apply(): Behavior[Update] = Behaviors.receiveMessage {
    case addSubscription: AddSubscription => addSubscriptionEmail(addSubscription)
    case opportunity: OpportunityAlert => sendOpportunityAlert(opportunity)
  }

  private def addSubscriptionEmail(subscription: AddSubscription): Behavior[Update] = {

    logger.trace(s"AddSubscription message received. Email: ${subscription.email}")

    val userEmail: String = subscription.email
    subscriptions += userEmail

    logger.info(s"Users email address successfully registered. Email: ${userEmail}")

    Behaviors.same
  }

  private def sendOpportunityAlert(opportunity: OpportunityAlert): Behavior[Update] = {

    logger.trace(s"OpportunityAlert message received. Path: ${opportunity.path}")

    // Generate message content
    val messageContent = prepareMessageContent(opportunity.path)

    logger.info(s"Trading opportunity path: $messageContent")

    // Send message to every subscription
    subscriptions.foreach((email: String) => mailerService.sendEmail(messageContent, email))

    Behaviors.same
  }

  private def prepareMessageContent(path: List[CoinContext]): String = {
    path.map((coinCheckpoint: CoinContext) => s"${coinCheckpoint.coinBaseId}[${coinCheckpoint.exchange}]")
      .mkString(" -> ")
  }
}
