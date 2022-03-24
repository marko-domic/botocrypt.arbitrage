package com.botocrypt.arbitrage.controllers

import com.botocrypt.arbitrage.dto.{SubscriptionCreatedDto, SubscriptionDto}
import com.botocrypt.arbitrage.service.SubscriptionService
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}


@Singleton
class SubscriptionController @Inject()(val controllerComponents: ControllerComponents,
                                       val subscriptionService: SubscriptionService) extends BaseController {

  implicit val subscriptionDtoJson: OFormat[SubscriptionDto] = Json.format[SubscriptionDto]
  implicit val subscriptionCreatedDtoJson: OFormat[SubscriptionCreatedDto] = Json.format[SubscriptionCreatedDto]

  def addSubscription(): Action[AnyContent] = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson

    val maybeSubscriptionDto: Option[SubscriptionDto] = jsonObject.flatMap(Json.fromJson[SubscriptionDto](_).asOpt)

    maybeSubscriptionDto match {
      case Some(subscriptionDto) =>
        Created(Json.toJson(subscriptionService.addSubscription(subscriptionDto)))
      case None =>
        BadRequest
    }
  }
}
