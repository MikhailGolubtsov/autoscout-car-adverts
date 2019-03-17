package com.github.mikhailgolubtsov.autoscout.caradverts.persistence
import java.util.concurrent.ConcurrentHashMap

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{AdvertId, CarAdvert}
import com.github.mikhailgolubtsov.autoscout.caradverts.persistence.CarAdvertRepository.DuplicateIdError

import scala.concurrent.Future

class CarAdvertInMemoryRepository extends CarAdvertRepository {

  private val carAdverts = new ConcurrentHashMap[AdvertId, CarAdvert]()

  override def createCarAdvert(carAdvert: CarAdvert): Future[Option[CarAdvertRepository.DuplicateIdError]] = {
    Future.successful {
      val existingAdvert = carAdverts.putIfAbsent(carAdvert.id, carAdvert)
      if (existingAdvert == null) {
        None
      } else {
        Some(DuplicateIdError(carAdvert.id))
      }
    }
  }

  override def getCarAdvertById(advertId: AdvertId): Future[Option[CarAdvert]] = {
    Future.successful {
      Option(carAdverts.get(advertId))
    }
  }
}