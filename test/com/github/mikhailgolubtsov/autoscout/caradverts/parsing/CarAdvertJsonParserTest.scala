package com.github.mikhailgolubtsov.autoscout.caradverts.parsing

import java.time.{LocalDate, Month}
import java.util.UUID

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{CarAdvert, FuelType}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsError, JsSuccess, Json}
import CarAdvertJsonParser._

class CarAdvertJsonParserTest extends WordSpec with MustMatchers {

  "Json parser of car advert request" should {
    "parse successfully json request for a new car" in {
      val jsonStr =
        """
          |{
          |  "id": "8d49c3f5-7637-4528-809f-0bed8f72e549",
          |  "title": "Audi",
          |  "fuel": "gasoline",
          |  "price": 10000,
          |  "new": true
          |}
        """.stripMargin
      val parsingResult = parseJsonToCarAdvert(jsonStr)
      parsingResult mustBe JsSuccess(CarAdvert(
        id = UUID.fromString("8d49c3f5-7637-4528-809f-0bed8f72e549"),
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
          |  "id": "8d49c3f5-7637-4528-809f-0bed8f72e549",
          |  "title": "Volkswagen",
          |  "fuel": "diesel",
          |  "price": 5000,
          |  "new": false,
          |  "mileage": 50000,
          |  "first_registration": "2015-06-01"
          |}
        """.stripMargin
      val parsingResult = parseJsonToCarAdvert(jsonStr)
      parsingResult mustBe JsSuccess(CarAdvert(
        id = UUID.fromString("8d49c3f5-7637-4528-809f-0bed8f72e549"),
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
          |  "id": "8d49c3f5-7637-4528-809f-0bed8f72e549",
          |  "title": "Tesla",
          |  "fuel": "electric",
          |  "price": 35000,
          |  "new": true
          |}
        """.stripMargin
      val parsingResult = parseJsonToCarAdvert(jsonStr)
      parsingResult mustBe a[JsError]
    }
  }

  private def parseJsonToCarAdvert(jsonStr: String) = {
    Json.fromJson[CarAdvert](Json.parse(jsonStr))
  }
}
