package com.github.mikhailgolubtsov.autoscout.caradverts.persistence

import java.util.UUID

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{AdvertId, CarAdvert, FuelType}
import com.github.mikhailgolubtsov.autoscout.caradverts.persistence.CarAdvertRepository.{CarAdvertNotFoundError, DuplicateIdError}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, OptionValues, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global

class CarAdvertInMemoryRepositoryTest extends WordSpec with MustMatchers with ScalaFutures with OptionValues {

  "Implementation of car advert repository" should {
    "return nothing if non existing id is request" in {
      val repository = new CarAdvertInMemoryRepository()
      whenReady(repository.getCarAdvertById(someId())) {
        _ mustBe None
      }
    }
    "create a new car advert and return no error" in {
      val repository = new CarAdvertInMemoryRepository()
      whenReady(repository.createCarAdvert(carAdvert)) { maybeError =>
        maybeError mustBe None
      }
    }
    "return duplicate error if creating an advert with the same id" in {
      val repository = new CarAdvertInMemoryRepository()

      val result = for {
        _ <- repository.createCarAdvert(carAdvert)
        maybeError <- repository.createCarAdvert(carAdvert)
      } yield maybeError

      whenReady(result) { maybeError =>
        maybeError mustBe Some(DuplicateIdError(carAdvert.id))
      }
    }
    "return created car advert by id" in {
      val repository = new CarAdvertInMemoryRepository()

      val result = for {
        _ <- repository.createCarAdvert(carAdvert)
        carAdvertMaybe <- repository.getCarAdvertById(carAdvert.id)
      } yield carAdvertMaybe

      whenReady(result) { carAdvertMaybe =>
        carAdvertMaybe.value mustBe carAdvert
      }
    }

    "return error if deleting not existing car advert" in {
      val repository = new CarAdvertInMemoryRepository()

      val id = someId()
      whenReady(repository.deleteCarAdvertById(id)) { errorMaybe =>
        errorMaybe.value mustBe CarAdvertNotFoundError(id)
      }
    }

    "return no error when deleting existing car advert" in {
      val repository = new CarAdvertInMemoryRepository()

      val result = for {
        _ <- repository.createCarAdvert(carAdvert)
        maybeError <- repository.deleteCarAdvertById(carAdvert.id)
      } yield maybeError

      whenReady(result) { maybeError =>
        maybeError mustBe None
      }
    }

    "find no car advert after deleting it" in {
      val repository = new CarAdvertInMemoryRepository()

      val result = for {
        _ <- repository.createCarAdvert(carAdvert)
        _ <- repository.deleteCarAdvertById(carAdvert.id)
        carAdvertMaybe <- repository.getCarAdvertById(carAdvert.id)
      } yield carAdvertMaybe

      whenReady(result) { carAdvertMaybe =>
        carAdvertMaybe mustBe None
      }
    }

    "return error if updating not existing car advert" in {
      val repository = new CarAdvertInMemoryRepository()
      whenReady(repository.updateCarAdvert(carAdvert)) { maybeError =>
        maybeError.value mustBe CarAdvertNotFoundError(carAdvert.id)
      }
    }

    "return no error if updating existing car advert" in {
      val repository = new CarAdvertInMemoryRepository()

      val updatedCarAdvert = carAdvert.copy(price = carAdvert.price + 1)
      val result = for {
        _ <- repository.createCarAdvert(carAdvert)
        maybeError <- repository.updateCarAdvert(updatedCarAdvert)
      } yield maybeError

      whenReady(result) { maybeError =>
        maybeError mustBe None
      }
    }

    "return updated car advert after updating it" in {
      val repository = new CarAdvertInMemoryRepository()

      val updatedCarAdvert = carAdvert.copy(price = carAdvert.price + 1)
      val result = for {
        _ <- repository.createCarAdvert(carAdvert)
        _ <- repository.updateCarAdvert(updatedCarAdvert)
        maybeCarAdvert <- repository.getCarAdvertById(carAdvert.id)
      } yield maybeCarAdvert

      whenReady(result) { maybeCarAdvert =>
        maybeCarAdvert.value mustBe updatedCarAdvert
      }

    }

    "return empty list of car adverts if none is created" in {
      val repository = new CarAdvertInMemoryRepository()

      whenReady(repository.getAllCarAdverts()) { allAdverts =>
        allAdverts mustBe List()
      }
    }

    "return list of car adverts with a created car advert" in {
      val repository = new CarAdvertInMemoryRepository()

      val result = for {
        _ <- repository.createCarAdvert(carAdvert)
        allAdverts <- repository.getAllCarAdverts()
      } yield {
        allAdverts
      }
      whenReady(result) { allAdverts =>
        allAdverts mustBe List(carAdvert)
      }
    }
  }

  val carAdvert: CarAdvert = {
    CarAdvert(
      id = someId(),
      title = "Audi",
      fuel = FuelType.Gasoline,
      price = 10000,
      isNew = true,
      mileage = None,
      firstRegistrationDate = None
    )
  }

  def someId(): AdvertId = UUID.randomUUID()
}
