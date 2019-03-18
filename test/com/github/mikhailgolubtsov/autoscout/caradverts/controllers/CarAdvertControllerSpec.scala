package com.github.mikhailgolubtsov.autoscout.caradverts.controllers

import java.time.{LocalDate, Month}
import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.mikhailgolubtsov.autoscout.caradverts.domain.CarAdvertService.CreationError
import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{AdvertId, CarAdvert, CarAdvertService, FuelType}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.mockito.Mockito._
import play.api.libs.json.{JsValue, Json}

class CarAdvertControllerSpec extends PlaySpec with Results with MockitoSugar {

  implicit val sys: ActorSystem = ActorSystem("CarAdvertControllerSpec")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val validJsonRequest =
    """
        {
          "id": "8d49c3f5-7637-4528-809f-0bed8f72e549",
          "title": "Audi",
          "fuel": "gasoline",
          "price": 10000,
          "new": true
        }
        """

  "Car advert controller creating car advert" should {

    "return 200 if service return no error" in {
      val controller = controllerWith(happyCaseService)
      val request = requestWith(validJsonRequest)
      val result = controller.createCarAdvert(request)

      status(result) mustBe 200
    }

    "return 400 if response is not json" in {
      val controller = controllerWith(happyCaseService)
      val request = FakeRequest().withBody("not json")
      val result = controller.createCarAdvert(request)

      status(result) mustBe 400
      jsonErrorMessage(contentAsJson(result)) mustBe "Json request is expected"
    }

    "return 400 if there is a missing field (id) in the request" in {
      val controller = controllerWith(happyCaseService)
      val jsonRequest =
        """
          {
            "title": "Audi",
            "fuel": "gasoline",
            "price": 10000,
            "new": true
          }
        """
      val request = requestWith(jsonRequest)
      val result = controller.createCarAdvert(request)

      status(result) mustBe 400
      jsonErrorMessage(contentAsJson(result)).startsWith("Invalid format of json request") mustBe true
    }

    "return 409 if service returns that car advert with given id already exists" in {
      val id = UUID.fromString("8d49c3f5-7637-4528-809f-0bed8f72e549")
      val service = mockedServiceWithMaybeError(maybeError = Some(CreationError.DuplicateCarAdvertId(id)))
      val controller = controllerWith(service)
      val jsonRequest =
        """
        {
          "id": "8d49c3f5-7637-4528-809f-0bed8f72e549",
          "title": "Audi",
          "fuel": "gasoline",
          "price": 10000,
          "new": true
        }
        """
      val request = requestWith(jsonRequest)
      val result = controller.createCarAdvert(request)


      status(result) mustBe 409
      jsonErrorMessage(contentAsJson(result)) mustBe
        s"Car advert with '8d49c3f5-7637-4528-809f-0bed8f72e549' already exists"
    }

    "return 500 and error message if service throws an exception" in {
      val service = mockedServiceWith(Future.failed(new Exception("some problem")))
      val controller = controllerWith(service)
      val request = requestWith(validJsonRequest)
      val result = controller.createCarAdvert(request)

      status(result) mustBe 500
      jsonErrorMessage(contentAsJson(result)) mustBe "Unexpected error: some problem"
    }

  }

  "Car advert controller requesting car advert by id" should {
  "return 200 and proper json body when existing car advert is requested" in {
      val id = UUID.fromString("8d49c3f5-7637-4528-809f-0bed8f72e549")
      var carAdvert =
        CarAdvert(
          id = id,
          title = "Volkswagen",
          fuel = FuelType.Diesel,
          price = 5000,
          isNew = false,
          mileage = Some(50000),
          firstRegistrationDate = Some(LocalDate.of(2015, Month.JUNE, 1))
        )
      val service = mockedServiceWith(requestedId = id, Future.successful(Some(carAdvert)))
      val controller = controllerWith(service)
      val result = controller.getCarAdvertById(id.toString)(FakeRequest())

      val expectedJson = Json.parse("""
        {
          "id": "8d49c3f5-7637-4528-809f-0bed8f72e549",
          "title": "Volkswagen",
          "fuel": "diesel",
          "price": 5000,
          "new": false,
          "mileage": 50000,
          "first_registration": "2015-06-01"
        }
        """)
      status(result) mustBe 200
      contentAsJson(result) mustBe expectedJson
    }

    "return 404 if car advert with request id doesn't exist" in {
      val id = UUID.fromString("8d49c3f5-7637-4528-809f-0bed8f72e549")
      val service = mockedServiceWith(requestedId = id, Future.successful(None))
      val controller = controllerWith(service)
      val result = controller.getCarAdvertById(id.toString)(FakeRequest())

      status(result) mustBe 404
    }

    "return 500 and error message if service throws an exception" in {
      val id = UUID.fromString("8d49c3f5-7637-4528-809f-0bed8f72e549")
      val service = mockedServiceWith(id, Future.failed(new Exception("some problem")))
      val controller = controllerWith(service)
      val result = controller.getCarAdvertById(id.toString)(FakeRequest())

      status(result) mustBe 500
      jsonErrorMessage(contentAsJson(result)) mustBe "Unexpected error: some problem"
    }
  }

  private def requestWith(jsonRequest: String) = {
    FakeRequest().withJsonBody(Json.parse(jsonRequest))
  }

  private def controllerWith(service: CarAdvertService) = {
    new CarAdvertController(Helpers.stubControllerComponents(), service)
  }

  def jsonErrorMessage(json: JsValue) = {
    (json \ "message").as[String]
  }

  def happyCaseService: CarAdvertService = {
    mockedServiceWithMaybeError(maybeError = None)
  }

  def mockedServiceWithMaybeError(maybeError: Option[CreationError]): CarAdvertService = {
    mockedServiceWith(result = Future.successful(maybeError))
  }

  def mockedServiceWith(result: Future[Option[CreationError]]): CarAdvertService = {
    val service = mock[CarAdvertService]
    when(service.createCarAdvert(any[CarAdvert])).thenReturn(result)
    service
  }

  def mockedServiceWith(requestedId: AdvertId, result: Future[Option[CarAdvert]]): CarAdvertService = {
    val service = mock[CarAdvertService]
    when(service.getCarAdvertById(ArgumentMatchers.eq(requestedId))).thenReturn(result)
    service
  }
}