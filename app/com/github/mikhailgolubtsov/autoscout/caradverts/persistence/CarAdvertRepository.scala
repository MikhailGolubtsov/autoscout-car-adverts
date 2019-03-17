package com.github.mikhailgolubtsov.autoscout.caradverts.persistence

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{AdvertId, CarAdvertCreateRequest}
import com.github.mikhailgolubtsov.autoscout.caradverts.persistence.CarAdvertRepository.CreationError

import scala.concurrent.Future

trait CarAdvertRepository {
  def createCarAdvert(createRequest: CarAdvertCreateRequest): Future[Option[CreationError]]
}

object CarAdvertRepository {

  sealed class CreationError

  object CreationError {

    case class DuplicateId(advertId: AdvertId) extends CreationError

    case class PersistenceError(exception: Exception) extends CreationError

  }
}
