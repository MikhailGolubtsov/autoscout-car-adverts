package com.github.mikhailgolubtsov.autoscout.caradverts.domain

import java.time._
import CarAdvertCreationError._

import org.scalatest.{MustMatchers, WordSpec}

class CarAdvertCreateRequestValidatorSpec extends WordSpec with MustMatchers {

  "Validator of car advert creation request" should {
    "return no errors for valid request for a new car" in new TestContext {
      val request = validNewCarRequest
      val errors = validator.validate(request)
      errors mustBe Set()
    }
    "return no errors for valid request for a used car" in new TestContext {
      val request = validUsedCarRequest
      val errors = validator.validate(request)
      errors mustBe Set()
    }
    "return error if price is 0" in new TestContext {
      val request = validUsedCarRequest.copy(price = 0)
      val errors = validator.validate(request)
      errors mustBe Set(InvalidPrice)
    }
    "return error if first registration date is tomorrow" in new TestContext {
      val tomorrowDate: LocalDate = today.plusDays(1)
      val request = validUsedCarRequest.copy(firstRegistrationDate = Some(tomorrowDate))
      val errors = validator.validate(request)
      errors mustBe Set(RegistrationDateInFuture)
    }
    "return no errors if first registration date is today, why not" in new TestContext {
      val request = validUsedCarRequest.copy(firstRegistrationDate = Some(today))
      val errors = validator.validate(request)
      errors mustBe Set()
    }
    "return error if title is empty" in new TestContext {
      val request = validNewCarRequest.copy(title = " ")
      val errors = validator.validate(request)
      errors mustBe Set(TitleIsEmpty)
    }
    "return error if there is registration date for a new car" in new TestContext {
      val request = validNewCarRequest.copy(firstRegistrationDate = Some(today))
      val errors = validator.validate(request)
      errors mustBe Set(NewCarHasRegistrationDate)
    }
    "return error if there is mileage for a new car" in new TestContext {
      val request = validNewCarRequest.copy(mileage = Some(10000))
      val errors = validator.validate(request)
      errors mustBe Set(NewCarHasMileage)
    }
    "return error if there is no mileage for a used car" in new TestContext {
      val request = validUsedCarRequest.copy(mileage = None)
      val errors = validator.validate(request)
      errors mustBe Set(UsedCarHasNoMileage)
    }
    "return error if there is no first registration date for a used car" in new TestContext {
      val request = validUsedCarRequest.copy(firstRegistrationDate = None)
      val errors = validator.validate(request)
      errors mustBe Set(UsedCarHasNoRegistrationDate)
    }
    "return all errors if several checks are violated" in new TestContext {
      val request = validNewCarRequest.copy(title = "", price = -1)
      val errors = validator.validate(request)
      errors mustBe Set(TitleIsEmpty, InvalidPrice)
    }
  }

  trait TestContext {

    val today: LocalDate = LocalDate.of(2019, Month.JANUARY, 1)

    def validNewCarRequest = {
      CarAdvertCreateRequest(
        title = "Audi",
        fuel = FuelType.Gasoline,
        price = 10000,
        isNew = true,
        mileage = None,
        firstRegistrationDate = None
      )
    }

    def validUsedCarRequest = {
      validNewCarRequest.copy(
        isNew = false,
        mileage = Some(50000),
        firstRegistrationDate = Some(today.minusYears(5)))
    }

    def validator: CarAdvertCreateRequestValidator = {
      val zone = ZoneId.of("UTC")
      val clock: Clock = Clock.fixed(today.atStartOfDay(zone).toInstant, zone)
      new CarAdvertCreateRequestValidator(clock)
    }

  }

}