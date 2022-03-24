package com.botocrypt.arbitrage.modules

import com.botocrypt.arbitrage.service.SubscriptionService
import com.google.inject.AbstractModule

import javax.inject.Singleton

class ServiceModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[SubscriptionService]).in(classOf[Singleton])
  }
}
