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

  def getAllCarAdverts(sortKeyMaybe: Option[SortKey]): Future[List[CarAdvert]] = {
    val sortKey = sortKeyMaybe.getOrElse(SortKey.Id)

    repository.getAllCarAdverts().map { allCarAdverts =>
      sortKey match {
        case SortKey.Id                    => allCarAdverts.sortBy(_.id)
        case SortKey.Title                 => allCarAdverts.sortBy(_.title)
        case SortKey.Price                 => allCarAdverts.sortBy(_.price)
        case SortKey.FuelType              => allCarAdverts.sortBy(_.fuel)
        case SortKey.IsNew                 => allCarAdverts.sortBy(_.isNew)
        case SortKey.Mileage               => allCarAdverts.sortBy(_.mileage)
        case SortKey.FirstRegistrationDate => allCarAdverts.sortBy(_.firstRegistrationDate)
      }
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

  sealed abstract class SortKey
  object SortKey {
    case object Id extends SortKey
    case object Title extends SortKey
    case object Price extends SortKey
    case object FuelType extends SortKey
    case object IsNew extends SortKey
    case object Mileage extends SortKey
    case object FirstRegistrationDate extends SortKey
  }
}
