package com.github.mikhailgolubtsov.autoscout.caradverts.controllers

import java.util.UUID

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.CarAdvertService.{CreationError, SortKey, UpdateError}
import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{CarAdvert, CarAdvertService, CarAdvertValidationError}
import com.github.mikhailgolubtsov.autoscout.caradverts.dto.CarAdvertUpdateRequest
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
    handleJsonCarAdvertRequest[CarAdvert](request, createCarAdvertWithErrorHandling)
  }

  def updateCarAdvert(id: String) = Action.async { request =>
    handleJsonCarAdvertRequest[CarAdvertUpdateRequest](request, updateCarAdvertWithErrorHandling(id, _))
  }

  def getAllCarAdverts(sortKey: Option[String]) = Action.async { _ =>
    val errorOrParsedSortKey: Either[String, Option[SortKey]] = parseSortKey(sortKey)
    errorOrParsedSortKey match {
      case Right(maybeSortKey) =>
        getAllCarAdvertsResponse(maybeSortKey)
      case Left(errorMessage) =>
        Future.successful {
          BadRequest(jsonProblem(errorMessage))
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

  private def handleJsonCarAdvertRequest[T](
      request: Request[AnyContent],
      processRequestF: T => Future[Result]
  )(implicit reads: Reads[T]): Future[Result] = {
    request.body.asJson match {
      case Some(jsonRequest) => {
        Json.fromJson[T](jsonRequest) match {
          case JsSuccess(request, _) => {
            processRequestF(request)
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

  private def updateCarAdvertWithErrorHandling(id: String, updateRequest: CarAdvertUpdateRequest): Future[Result] = {
    def updateResultToResponse(maybeCreationError: Option[UpdateError]) = {
      maybeCreationError match {
        case None => Ok
        case Some(UpdateError.InvalidRequest(errors)) => {
          validationErrorsToResult(errors)
        }
        case Some(UpdateError.NotFoundCarAdvert(advertId)) =>
          NotFound(jsonProblem(s"Car advert id='${advertId}' is not found"))
      }
    }

    val carAdvert = CarAdvert(
      id = UUID.fromString(id),
      title = updateRequest.title,
      fuel = updateRequest.fuel,
      price = updateRequest.price,
      isNew = updateRequest.isNew,
      mileage = updateRequest.mileage,
      firstRegistrationDate = updateRequest.firstRegistrationDate
    )

    carAdvertService
      .updateCarAdvert(carAdvert)
      .map(updateResultToResponse)
      .recover(recoverException)
  }

  private def createCarAdvertWithErrorHandling(carAdvert: CarAdvert): Future[Result] = {
    def creationResultToResponse(maybeCreationError: Option[CreationError]) = {
      maybeCreationError match {
        case None => Ok
        case Some(CreationError.InvalidRequest(errors)) => {
          validationErrorsToResult(errors)
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

  private def parseSortKey(sortKey: Option[String]) = {
    sortKey match {
      case Some(sortKey) =>
        sortKey match {
          case "id"                 => Right(Some(SortKey.Id))
          case "title"              => Right(Some(SortKey.Title))
          case "price"              => Right(Some(SortKey.Price))
          case "new"                => Right(Some(SortKey.IsNew))
          case "fuel"               => Right(Some(SortKey.FuelType))
          case "mileage"            => Right(Some(SortKey.Mileage))
          case "first_registration" => Right(Some(SortKey.FirstRegistrationDate))
          case other                => Left(s"Invalid sort key - $other")
        }
      case None => Right(None)
    }
  }

  private def getAllCarAdvertsResponse(maybeSortKey: Option[SortKey]): Future[Result] = {
    carAdvertService
      .getAllCarAdverts(maybeSortKey)
      .map({ allCarAdverts =>
        Ok(Json.toJson(allCarAdverts))
      })
      .recover(recoverException)
  }

  private def validationErrorsToResult(errors: Set[CarAdvertValidationError]) = {
    val errorMsg = errors.map(_.errorMessage).mkString("; ")
    BadRequest(jsonProblem(s"Domain rules are violated: $errorMsg"))
  }

  private val recoverException: PartialFunction[Throwable, Result] = {
    case e: Throwable =>
      InternalServerError(jsonProblem(s"Unexpected error: ${e.getMessage}"))
  }

  private def jsonProblem(message: String): JsValue = {
    JsObject(Map("message" -> JsString(message)))
  }

}
