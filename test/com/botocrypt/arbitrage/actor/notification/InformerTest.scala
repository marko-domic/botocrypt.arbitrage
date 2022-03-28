package com.botocrypt.arbitrage.actor.notification

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.botocrypt.arbitrage.actor.notification.Informer.{AddSubscription, CoinContext, OpportunityAlert}
import com.botocrypt.arbitrage.service.MailerService
import org.mockito.{ArgumentMatchers, MockitoSugar}
import org.scalatest.wordspec.AnyWordSpecLike

class InformerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with MockitoSugar {

  "Informer" must {

    "add subscription and send opportunity alert" in {

      // Create mock service and testing actor
      val mailerService: MailerService = mock[MailerService]
      val informer = testKit.spawn(Informer(mailerService), "informer")

      // Create email values
      val email1: String = "test1@gmail.com"
      val email2: String = "test2@outlook.com"

      // Create subscription messages
      val subscription1: AddSubscription = AddSubscription(email1)
      val subscription2: AddSubscription = AddSubscription(email2)

      // Send subscription messages
      informer ! subscription1
      informer ! subscription2

      // Create opportunity path
      val coinContext1: CoinContext = CoinContext("USD", "CEX.IO")
      val coinContext2: CoinContext = CoinContext("BTC", "CEX.IO")
      val coinContext3: CoinContext = CoinContext("BTC", "Binance")
      val coinContext4: CoinContext = CoinContext("USD", "Binance")
      val path: List[CoinContext] = List(coinContext1, coinContext2, coinContext3, coinContext4)

      // Create opportunity message
      val alert: OpportunityAlert = OpportunityAlert(path)

      // Mail content
      val mailContent: String = "USD[CEX.IO] -> BTC[CEX.IO] -> BTC[Binance] -> USD[Binance]"

      // Setup mock
      doReturn("Success").when(mailerService).sendEmail(ArgumentMatchers.eq(mailContent),
        ArgumentMatchers.eq(email1))
      doReturn("Success").when(mailerService).sendEmail(ArgumentMatchers.eq(mailContent),
        ArgumentMatchers.eq(email2))

      // Send opportunity alert
      informer ! alert

      // Validate sending mail
      verify(mailerService, timeout(2000)).sendEmail(ArgumentMatchers.eq(mailContent),
        ArgumentMatchers.eq(email1))
      verify(mailerService, timeout(2000)).sendEmail(ArgumentMatchers.eq(mailContent),
        ArgumentMatchers.eq(email2))
    }
  }
}
