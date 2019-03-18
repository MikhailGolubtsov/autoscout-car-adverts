package com.github.mikhailgolubtsov.autoscout.caradverts.controllers

import java.util.UUID

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.CarAdvertService.CreationError
import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{CarAdvert, CarAdvertService}
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import com.github.mikhailgolubtsov.autoscout.caradverts.parsing.CarAdvertJsonParser._
import com.github.mikhailgolubtsov.autoscout.caradverts.persistence.CarAdvertRepository.CarAdvertNotFoundError

@Singleton
class CarAdvertController @Inject()(cc: ControllerComponents, carAdvertService: CarAdvertService)(
    implicit ec: ExecutionContext
) extends AbstractController(cc) {

  def createCarAdvert = Action.async { request =>
    request.body.asJson match {
      case Some(jsonRequest) => {
        Json.fromJson[CarAdvert](jsonRequest) match {
          case JsSuccess(carAdvert, _) => {
            createCarAdvertWithErrorHandling(carAdvert)
          }
          case JsError(errors) =>
            Future.successful {
              BadRequest(jsonProblem(s"Invalid format of json request: ${errors}"))
            }
        }
      }
      case None =>
        Future.successful {
          BadRequest(jsonProblem("Json request is expected"))
        }
    }
  }

  def getCarAdvertById(id: String) = Action.async { request =>
    val advertId = UUID.fromString(id)
    carAdvertService
      .getCarAdvertById(advertId)
      .map({
        case None            => NotFound
        case Some(carAdvert) => Ok(Json.toJson(carAdvert))
      })
      .recover(recoverException)
  }

  def deleteCarAdvertById(id: String) = Action.async { request =>
    val advertId = UUID.fromString(id)
    carAdvertService
      .deleteCarAdvertById(advertId)
      .map({
        case Some(CarAdvertNotFoundError(_)) =>
          NotFound(jsonProblem(s"Car advert id='$id' is not found"))
        case None => Ok
      })
      .recover(recoverException)
  }

  private def createCarAdvertWithErrorHandling(carAdvert: CarAdvert): Future[Result] = {
    def creationResultToResponse(maybeCreationError: Option[CreationError]) = {
      maybeCreationError match {
        case None => Ok
        case Some(CreationError.InvalidRequest(errors)) => {
          val errorMsg = errors.map(_.errorMessage).mkString("; ")
          BadRequest(jsonProblem(s"Domain rules are violated: $errorMsg"))
        }
        case Some(CreationError.DuplicateCarAdvertId(advertId)) =>
          Conflict(jsonProblem(s"Car advert with '${advertId}' already exists"))
      }
    }

    carAdvertService
      .createCarAdvert(carAdvert)
      .map(creationResultToResponse)
      .recover(recoverException)
  }

  private val recoverException: PartialFunction[Throwable, Result] = {
    case e: Throwable =>
      InternalServerError(jsonProblem(s"Unexpected error: ${e.getMessage}"))
  }

  private def jsonProblem(message: String): JsValue = {
    JsObject(Map("message" -> JsString(message)))
  }

}
