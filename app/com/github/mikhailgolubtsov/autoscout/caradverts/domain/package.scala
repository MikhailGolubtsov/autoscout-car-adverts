package com.github.mikhailgolubtsov.autoscout.caradverts

import java.time.LocalDate
import java.util.UUID

package object domain {

  type AdvertId = UUID

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)
}
