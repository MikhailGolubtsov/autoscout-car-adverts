package com.github.mikhailgolubtsov.autoscout.caradverts.domain

sealed abstract class FuelType

object FuelType {

  case object Diesel extends FuelType

  case object Gasoline extends FuelType

  implicit val ordering: Ordering[FuelType] = Ordering.by(_.getClass.getName)

}
