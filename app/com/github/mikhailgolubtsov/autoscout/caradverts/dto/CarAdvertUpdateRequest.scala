package com.github.mikhailgolubtsov.autoscout.caradverts.dto

import java.time.LocalDate

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.FuelType

case class CarAdvertUpdateRequest(
    title: String,
    fuel: FuelType,
    price: Int,
    isNew: Boolean,
    mileage: Option[Int],
    firstRegistrationDate: Option[LocalDate]
)
