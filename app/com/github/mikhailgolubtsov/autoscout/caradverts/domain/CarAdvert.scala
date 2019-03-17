package com.github.mikhailgolubtsov.autoscout.caradverts.domain

import java.time.LocalDate

case class CarAdvert(
    id: AdvertId,
    title: String,
    fuel: FuelType,
    price: Int,
    isNew: Boolean,
    mileage: Option[Int],
    firstRegistrationDate: Option[LocalDate]
)
