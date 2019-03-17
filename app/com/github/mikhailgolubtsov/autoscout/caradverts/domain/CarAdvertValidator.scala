package com.github.mikhailgolubtsov.autoscout.caradverts.domain

import java.time.{Clock, LocalDate}
import CarAdvertValidationError._

class CarAdvertValidator(clock: Clock) {

  def validate(request: CarAdvert): Set[CarAdvertValidationError] = {
    val validationErrors = allChecks.flatMap(_.apply(request))
    validationErrors.toSet
  }

  private type ValidationCheck = CarAdvert => Option[CarAdvertValidationError]

  private val allChecks: Seq[ValidationCheck] = Seq(
    checkIf(InvalidPrice, { r =>
      r.price <= 0
    }),
    checkIf(NewCarHasRegistrationDate, { r =>
      r.isNew && r.firstRegistrationDate.isDefined
    }),
    checkIf(NewCarHasMileage, { r =>
      r.isNew && r.mileage.isDefined
    }),
    checkIf(UsedCarHasNoRegistrationDate, { r =>
      !r.isNew && r.firstRegistrationDate.isEmpty
    }),
    checkIf(UsedCarHasNoMileage, { r =>
      !r.isNew && r.mileage.isEmpty
    }),
    checkIf(TitleIsEmpty, { r =>
      r.title.trim.isEmpty
    }),
    checkIf(RegistrationDateInFuture, isRegistrationDateInFuture)
  )

  private def checkIf(
      errorToReturn: CarAdvertValidationError,
      conditionToCheck: CarAdvert => Boolean
  ): ValidationCheck = { request =>
    if (conditionToCheck(request)) {
      Some(errorToReturn)
    } else {
      None
    }
  }

  private def isRegistrationDateInFuture(request: CarAdvert): Boolean = {
    def currentDate: LocalDate = clock.instant().atZone(clock.getZone).toLocalDate
    request.firstRegistrationDate match {
      case Some(registrationDate) if registrationDate.compareTo(currentDate) > 0 =>
        true
      case _ => false
    }
  }
}

sealed trait CarAdvertValidationError {
  def errorMessage: String
}

object CarAdvertValidationError {

  case object InvalidPrice extends CarAdvertValidationError {
    override def errorMessage: String = "Price must be positive"
  }

  case object NewCarHasRegistrationDate extends CarAdvertValidationError {
    override def errorMessage: String = "For new cars registration date must be not present"
  }

  case object NewCarHasMileage extends CarAdvertValidationError {
    override def errorMessage: String = "For new cars mileage must be not present"
  }

  case object UsedCarHasNoRegistrationDate extends CarAdvertValidationError {
    override def errorMessage: String = "For used cars registration date is required"
  }

  case object UsedCarHasNoMileage extends CarAdvertValidationError {
    override def errorMessage: String = "For used cars mileage is required"
  }

  case object TitleIsEmpty extends CarAdvertValidationError {
    override def errorMessage: String = "Title must be not empty"
  }

  case object RegistrationDateInFuture extends CarAdvertValidationError {
    override def errorMessage: String = "Registration date cannot be a future date"
  }

}
