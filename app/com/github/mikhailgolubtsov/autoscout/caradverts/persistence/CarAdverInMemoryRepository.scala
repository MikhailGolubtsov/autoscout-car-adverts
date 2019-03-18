package com.github.mikhailgolubtsov.autoscout.caradverts.persistence
import java.util.concurrent.ConcurrentHashMap

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{AdvertId, CarAdvert}
import com.github.mikhailgolubtsov.autoscout.caradverts.persistence.CarAdvertRepository._

import scala.concurrent.Future
import collection.JavaConverters._

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

  override def deleteCarAdvertById(advertId: AdvertId): Future[Option[CarAdvertNotFoundError]] = {
    Future.successful {
      val removedAdvert = carAdverts.remove(advertId)
      if (removedAdvert == null) {
        Some(CarAdvertNotFoundError(advertId))
      } else {
        None
      }
    }
  }

  override def updateCarAdvert(carAdvert: CarAdvert): Future[Option[CarAdvertNotFoundError]] = {
    Future.successful {
      val previousState = carAdverts.put(carAdvert.id, carAdvert)
      if (previousState == null) {
        Some(CarAdvertNotFoundError(carAdvert.id))
      } else {
        None
      }
    }
  }

  override def getAllCarAdverts(): Future[List[CarAdvert]] = {
    Future.successful {
      carAdverts.values().asScala.toList
    }
  }
}
