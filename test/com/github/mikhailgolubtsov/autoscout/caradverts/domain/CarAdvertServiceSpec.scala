package com.github.mikhailgolubtsov.autoscout.caradverts.domain

import java.time.{Clock, LocalDate, Month, ZoneId}
import java.util.UUID

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.CarAdvertService.{CreationError, UpdateError}
import com.github.mikhailgolubtsov.autoscout.caradverts.domain.CarAdvertService.CreationError.DuplicateCarAdvertId
import com.github.mikhailgolubtsov.autoscout.caradverts.domain.CarAdvertValidationError.InvalidPrice
import com.github.mikhailgolubtsov.autoscout.caradverts.persistence.CarAdvertInMemoryRepository
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, OptionValues, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global

class CarAdvertServiceSpec extends WordSpec with MustMatchers with ScalaFutures with OptionValues {

  "Car advert service" should {
    "return no error on successful creation" in {
      val service = createService

      whenReady(service.createCarAdvert(invalidCarAdvert)) { maybeError =>
        maybeError.value mustBe CreationError.InvalidRequest(Set(InvalidPrice))
      }
    }
    "return failure if there is domain validation failure on creation" in {
      val service = createService

      whenReady(service.createCarAdvert(validCarAdvert)) { maybeError =>
        maybeError mustBe None
      }
    }
    "return duplication id error if storing the same car advert" in {
      val service = createService
      val result = for {
        _ <- service.createCarAdvert(validCarAdvert)
        maybeError <- service.createCarAdvert(validCarAdvert)
      } yield  maybeError

      whenReady(result) { maybeError =>
        maybeError.value mustBe DuplicateCarAdvertId(validCarAdvert.id)
      }
    }

    "create and return created car advert" in {
      val service = createService
      val carAdvertToCreate = validCarAdvert
      val result = for {
        _ <- service.createCarAdvert(carAdvertToCreate)
        maybeCarAdvert <- service.getCarAdvertById(carAdvertToCreate.id)
      } yield  maybeCarAdvert

      whenReady(result) { maybeCarAdvert =>
        maybeCarAdvert.value mustBe carAdvertToCreate
      }
    }

    "return not found error on update of non existing car advert" in {
      val service = createService

      whenReady(service.updateCarAdvert(validCarAdvert)) { maybeError =>
        maybeError.value mustBe UpdateError.NotFoundCarAdvert(validCarAdvert.id)
      }
    }

    "return validation error if a domain rule is violated" in {
      val service = createService

      whenReady(service.updateCarAdvert(invalidCarAdvert)) { maybeError =>
        maybeError.value mustBe UpdateError.InvalidRequest(Set(InvalidPrice))
      }
    }

    "return no error on update of existing car advert and get it by id" in {
      val service = createService

      val updateCarAdvert = validCarAdvert.copy(price = validCarAdvert.price + 1)
      val result = for {
        _ <- service.createCarAdvert(validCarAdvert)
        maybeUpdateError <- service.updateCarAdvert(updateCarAdvert)
        maybeUpdatedCarAdvert <- service.getCarAdvertById(updateCarAdvert.id)
      } yield  (maybeUpdateError, maybeUpdatedCarAdvert)

      whenReady(result) { case (maybeUpdateError, maybeUpdatedCarAdvert) =>
        maybeUpdateError mustBe None
        maybeUpdatedCarAdvert.value mustBe updateCarAdvert
      }
    }
  }

  private def createService: CarAdvertService = {
    val today: LocalDate = LocalDate.of(2019, Month.JANUARY, 1)
    val zone = ZoneId.of("UTC")
    val clock = Clock.fixed(today.atStartOfDay(zone).toInstant, zone)
    val service = new CarAdvertService(
      carAdvertValidator = new CarAdvertValidator(clock),
      repository = new CarAdvertInMemoryRepository())
    service
  }

  val validCarAdvert = {
    CarAdvert(
      id = UUID.fromString("8d49c3f5-7637-4528-809f-0bed8f72e549"),
      title = "Audi",
      fuel = FuelType.Gasoline,
      price = 10000,
      isNew = true,
      mileage = None,
      firstRegistrationDate = None
    )
  }

  val invalidCarAdvert = validCarAdvert.copy(price = -1)
}
