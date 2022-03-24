package com.botocrypt.arbitrage.service

import akka.actor.typed.ActorRef
import com.botocrypt.arbitrage.actor.notification.Informer
import com.botocrypt.arbitrage.actor.notification.Informer.AddSubscription
import com.botocrypt.arbitrage.dto.{SubscriptionCreatedDto, SubscriptionDto}

import javax.inject.{Inject, Singleton}

@Singleton
class SubscriptionService @Inject()(val informer: ActorRef[Informer.Update]) {

  def addSubscription(subscriptionDto: SubscriptionDto): SubscriptionCreatedDto = {

    informer ! AddSubscription(subscriptionDto.email)
    SubscriptionCreatedDto("Success", subscriptionDto.email)
  }
}
