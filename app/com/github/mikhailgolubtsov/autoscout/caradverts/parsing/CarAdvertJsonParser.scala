package com.github.mikhailgolubtsov.autoscout.caradverts.parsing

import java.time.LocalDate
import java.util.UUID

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{CarAdvert, FuelType}
import play.api.libs.json._
import play.api.libs.functional.syntax._

class CarAdvertJsonParser {

  private implicit val fuelReads: Reads[FuelType] = (json: JsValue) => {
    json.validate[String].flatMap {
      case "gasoline" => JsSuccess(FuelType.Gasoline)
      case "diesel"   => JsSuccess(FuelType.Diesel)
      case other      => JsError(s"Unsupported fuel type '${other}', valid values are 'diesel', 'gasoline'")
    }
  }

  private implicit val requestReads: Reads[CarAdvert] = (
    (__ \ "id").read[UUID] and
      (__ \ "title").read[String] and
      (__ \ "fuel").read[FuelType] and
      (__ \ "price").read[Int] and
      (__ \ "new").read[Boolean] and
      (__ \ "mileage").readNullable[Int] and
      (__ \ "first_registration").readNullable[LocalDate]
  )(CarAdvert)

  def parseRequest(json: JsValue): JsResult[CarAdvert] = {
    Json.fromJson[CarAdvert](json)
  }
}
