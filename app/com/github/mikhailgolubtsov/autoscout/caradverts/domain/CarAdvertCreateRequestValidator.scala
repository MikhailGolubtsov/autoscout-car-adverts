package com.github.mikhailgolubtsov.autoscout.caradverts.domain

import java.time.{Clock, LocalDate}
import CarAdvertCreationError._

class CarAdvertCreateRequestValidator(clock: Clock) {

  def validate(request: CarAdvertCreateRequest): Set[CarAdvertCreationError] = {
    val validationErrors = allChecks.flatMap(_.apply(request))
    validationErrors.toSet
  }

  private type ValidationCheck = CarAdvertCreateRequest => Option[CarAdvertCreationError]

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
      errorToReturn: CarAdvertCreationError,
      conditionToCheck: CarAdvertCreateRequest => Boolean
  ): ValidationCheck = { request =>
    if (conditionToCheck(request)) {
      Some(errorToReturn)
    } else {
      None
    }
  }

  private def isRegistrationDateInFuture(request: CarAdvertCreateRequest): Boolean = {
    def currentDate: LocalDate = clock.instant().atZone(clock.getZone).toLocalDate
    request.firstRegistrationDate match {
      case Some(registrationDate) if registrationDate.compareTo(currentDate) > 0 =>
        true
      case _ => false
    }
  }
}

sealed trait CarAdvertCreationError {
  def errorMessage: String
}

object CarAdvertCreationError {

  case object InvalidPrice extends CarAdvertCreationError {
    override def errorMessage: String = "Price must be positive"
  }

  case object NewCarHasRegistrationDate extends CarAdvertCreationError {
    override def errorMessage: String = "For new cars registration date must be not present"
  }

  case object NewCarHasMileage extends CarAdvertCreationError {
    override def errorMessage: String = "For new cars mileage must be not present"
  }

  case object UsedCarHasNoRegistrationDate extends CarAdvertCreationError {
    override def errorMessage: String = "For used cars registration date is required"
  }

  case object UsedCarHasNoMileage extends CarAdvertCreationError {
    override def errorMessage: String = "For used cars mileage is required"
  }

  case object TitleIsEmpty extends CarAdvertCreationError {
    override def errorMessage: String = "Title must be not empty"
  }

  case object RegistrationDateInFuture extends CarAdvertCreationError {
    override def errorMessage: String = "Registration date cannot be a future date"
  }

}
