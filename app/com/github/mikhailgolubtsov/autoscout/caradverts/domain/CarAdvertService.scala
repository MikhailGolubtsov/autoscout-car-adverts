package com.github.mikhailgolubtsov.autoscout.caradverts.domain

import com.github.mikhailgolubtsov.autoscout.caradverts.persistence.CarAdvertRepository
import CarAdvertService._
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

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
}

object CarAdvertService {
  sealed abstract class CreationError
  object CreationError {
    case class InvalidRequest(validationErrors: Set[CarAdvertValidationError]) extends CreationError
    case class DuplicateCarAdvertId(advertId: AdvertId) extends CreationError
  }

}
