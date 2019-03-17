package com.github.mikhailgolubtsov.autoscout.caradverts.persistence

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{AdvertId, CarAdvert}
import CarAdvertRepository._

import scala.concurrent.Future

trait CarAdvertRepository {
  def createCarAdvert(carAdvert: CarAdvert): Future[Option[DuplicateIdError]]

  def getCarAdvertById(advertId: AdvertId): Future[Option[CarAdvert]]
}

object CarAdvertRepository {
  case class DuplicateIdError(advertId: AdvertId)
}
