package com.github.mikhailgolubtsov.autoscout.caradverts.parsing

import java.time.LocalDate

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{CarAdvertCreateRequest, FuelType}
import play.api.libs.json._
import play.api.libs.functional.syntax._

class CarAdvertRequestJsonParser {

  private implicit val fuelReads: Reads[FuelType] = (json: JsValue) => {
    json.validate[String].flatMap {
      case "gasoline" => JsSuccess(FuelType.Gasoline)
      case "diesel" => JsSuccess(FuelType.Diesel)
      case other => JsError(s"Unsupported fuel type '${other}', valid values are 'diesel', 'gasoline'")
    }
  }

  private implicit val requestReads: Reads[CarAdvertCreateRequest] = (
      (__ \ "title").read[String] and
      (__ \ "fuel").read[FuelType] and
      (__ \ "price").read[Int] and
      (__ \ "new").read[Boolean] and
      (__ \ "mileage").readNullable[Int] and
      (__ \ "first_registration").readNullable[LocalDate]
    )(CarAdvertCreateRequest)

  def parseRequest(json: JsValue): JsResult[CarAdvertCreateRequest] = {
    Json.fromJson[CarAdvertCreateRequest](json)
  }
}
