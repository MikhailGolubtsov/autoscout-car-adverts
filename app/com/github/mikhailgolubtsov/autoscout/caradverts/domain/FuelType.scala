package com.github.mikhailgolubtsov.autoscout.caradverts.domain

sealed class FuelType

object FuelType {

  case object Diesel extends FuelType

  case object Gasoline extends FuelType

}
