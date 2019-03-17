package com.github.mikhailgolubtsov.autoscout.caradverts.domain

import com.github.mikhailgolubtsov.autoscout.caradverts.persistence.CarAdvertRepository
import CarAdvertService._

import scala.concurrent.Future

class CarAdvertService(repository: CarAdvertRepository, createRequestValidator: CarAdvertCreateRequestValidator) {

  def createCarAdvert(createRequest: CarAdvertCreateRequest): Future[Option[CreationError]] = {
    val validationErrors = createRequestValidator.validate(createRequest)
    if (validationErrors.isEmpty) {
      for {
        persistenceErrorMaybe <- repository.createCarAdvert(createRequest)
      } yield {
        persistenceErrorMaybe.map(CreationError.PersistenceError)
      }
    } else {
      Future.successful(Some(CreationError.InvalidRequest(validationErrors)))
    }
  }
}

object CarAdvertService {
  sealed class CreationError
  object CreationError {
    case class InvalidRequest(validationErrors: Set[CarAdvertValidationError]) extends CreationError
    case class PersistenceError(error: CarAdvertRepository.CreationError) extends CreationError
  }

}
