package com.botocrypt.arbitrage.service

import play.api.libs.mailer._

import javax.inject.Inject

class MailerService @Inject()(mailerClient: MailerClient) {

  def sendEmail(content: String, to: String) = {

    // Prepare email context
    val email = Email(
      subject = "Opportunity alert!",
      from = "Botocrypt Platform",
      to = Seq(to),
      bodyText = Some(content)
    )

    // Send an email
    mailerClient.send(email)
  }
}
