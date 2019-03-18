package com.github.mikhailgolubtsov.autoscout.caradverts.domain

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.CarAdvertService.UpdateError
import com.github.mikhailgolubtsov.autoscout.caradverts.persistence.CarAdvertRepository
import com.github.mikhailgolubtsov.autoscout.caradverts.persistence.CarAdvertRepository.CarAdvertNotFoundError
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import CarAdvertService._

@Singleton
class CarAdvertService @Inject()(repository: CarAdvertRepository, carAdvertValidator: CarAdvertValidator)(
    implicit ec: ExecutionContext
) {

  def createCarAdvert(carAdvert: CarAdvert): Future[Option[CreationError]] = {
    val validationErrors = carAdvertValidator.validate(carAdvert)
    if (validationErrors.isEmpty) {
      for {
        persistenceErrorMaybe <- repository.createCarAdvert(carAdvert)
      } yield {
        persistenceErrorMaybe.map(e => CreationError.DuplicateCarAdvertId(e.advertId))
      }
    } else {
      Future.successful(Some(CreationError.InvalidRequest(validationErrors)))
    }
  }

  def getCarAdvertById(advertId: AdvertId): Future[Option[CarAdvert]] = {
    repository.getCarAdvertById(advertId)
  }

  def deleteCarAdvertById(advertId: AdvertId): Future[Option[CarAdvertNotFoundError]] = {
    repository.deleteCarAdvertById(advertId)
  }

  def updateCarAdvert(carAdvert: CarAdvert): Future[Option[UpdateError]] = {
    val validationErrors = carAdvertValidator.validate(carAdvert)
    if (validationErrors.isEmpty) {
      for {
        maybeNonFoundError <- repository.updateCarAdvert(carAdvert)
      } yield {
        maybeNonFoundError.map(e => UpdateError.NotFoundCarAdvert(e.advertId))
      }
    } else {
      Future.successful(Some(UpdateError.InvalidRequest(validationErrors)))
    }
  }
}

object CarAdvertService {
  sealed abstract class CreationError
  object CreationError {
    case class InvalidRequest(validationErrors: Set[CarAdvertValidationError]) extends CreationError
    case class DuplicateCarAdvertId(advertId: AdvertId) extends CreationError
  }

  sealed abstract class UpdateError
  object UpdateError {
    case class InvalidRequest(validationErrors: Set[CarAdvertValidationError]) extends UpdateError
    case class NotFoundCarAdvert(advertId: AdvertId) extends UpdateError
  }

}
