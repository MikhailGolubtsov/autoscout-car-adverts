package com.github.mikhailgolubtsov.autoscout.caradverts.parsing

import java.time.{LocalDate, Month}

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{CarAdvertCreateRequest, FuelType}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsError, JsSuccess, Json}

class CarAdvertRequestJsonParserTest extends WordSpec with MustMatchers {

  "Json parser of car advert request" should {
    "parse successfully json request for a new car" in {
      val jsonStr =
        """
          |{
          |  "title": "Audi",
          |  "fuel": "gasoline",
          |  "price": 10000,
          |  "new": true
          |}
        """.stripMargin
      val parsingResult = new CarAdvertRequestJsonParser().parseRequest(Json.parse(jsonStr))
      parsingResult mustBe JsSuccess(CarAdvertCreateRequest(
        title = "Audi",
        fuel = FuelType.Gasoline,
        price = 10000,
        isNew = true,
        mileage = None,
        firstRegistrationDate = None
      ))
    }
    "parse successfully json request for a used car" in {
      val jsonStr =
        """
          |{
          |  "title": "Volkswagen",
          |  "fuel": "diesel",
          |  "price": 5000,
          |  "new": false
          |  "mileage": 50000,
          |  "first_registration": "2015-06-01"
          |}
        """.stripMargin
      val parsingResult = new CarAdvertRequestJsonParser().parseRequest(Json.parse(jsonStr))
      parsingResult mustBe JsSuccess(CarAdvertCreateRequest(
        title = "Volkswagen",
        fuel = FuelType.Diesel,
        price = 5000,
        isNew = false,
        mileage = Some(50000),
        firstRegistrationDate = Some(LocalDate.of(2015, Month.JUNE, 1))
      ))
    }
    "return parsing error for unsupported fuel type (electric)" in {
      val jsonStr =
        """
          |{
          |  "title": "Tesla",
          |  "fuel": "electric",
          |  "price": 35000,
          |  "new": true
          |}
        """.stripMargin
      val parsingResult = new CarAdvertRequestJsonParser().parseRequest(Json.parse(jsonStr))
      parsingResult mustBe a[JsError]
    }
  }
}
