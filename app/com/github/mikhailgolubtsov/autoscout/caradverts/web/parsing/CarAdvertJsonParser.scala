package com.github.mikhailgolubtsov.autoscout.caradverts.web.parsing

import java.time.LocalDate
import java.util.UUID

import com.github.mikhailgolubtsov.autoscout.caradverts.domain.FuelType.{Diesel, Gasoline}
import com.github.mikhailgolubtsov.autoscout.caradverts.domain.{CarAdvert, FuelType}
import com.github.mikhailgolubtsov.autoscout.caradverts.web.dto.CarAdvertUpdateRequest
import play.api.libs.json._
import play.api.libs.functional.syntax._

object CarAdvertJsonParser {

  implicit val fuelFormat: Format[FuelType] = new Format[FuelType] {
    override def reads(json: JsValue): JsResult[FuelType] = {
      json.validate[String].flatMap {
        case "gasoline" => JsSuccess(FuelType.Gasoline)
        case "diesel"   => JsSuccess(FuelType.Diesel)
        case other      => JsError(s"Unsupported fuel type '${other}', valid values are 'diesel', 'gasoline'")
      }
    }

    override def writes(fuelType: FuelType): JsValue = {
      JsString {
        fuelType match {
          case Gasoline => "gasoline"
          case Diesel   => "diesel"
        }
      }
    }
  }

  implicit val carAdvertUpdateRequestFormat: Format[CarAdvertUpdateRequest] = (
    (__ \ "title").format[String] and
      (__ \ "fuel").format[FuelType] and
      (__ \ "price").format[Int] and
      (__ \ "new").format[Boolean] and
      (__ \ "mileage").formatNullable[Int] and
      (__ \ "first_registration").formatNullable[LocalDate]
  )(CarAdvertUpdateRequest.apply, unlift(CarAdvertUpdateRequest.unapply))

  implicit val carAdvertFormat: Format[CarAdvert] = (
    (__ \ "id").format[UUID] and
      (__ \ "title").format[String] and
      (__ \ "fuel").format[FuelType] and
      (__ \ "price").format[Int] and
      (__ \ "new").format[Boolean] and
      (__ \ "mileage").formatNullable[Int] and
      (__ \ "first_registration").formatNullable[LocalDate]
  )(CarAdvert.apply, unlift(CarAdvert.unapply))
}
