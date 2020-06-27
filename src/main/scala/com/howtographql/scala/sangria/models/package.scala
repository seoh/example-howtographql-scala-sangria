package com.howtographql.scala.sangria

import akka.http.scaladsl.model.DateTime
import sangria.validation.Violation

package object models {
  case class Link(id: Int, url: String, description: String, createdAt: DateTime)
  case class User(id: Int, name: String, email: String, password: String, createdAt: DateTime = DateTime.now)

  case object DateTimeCoerceViolation extends Violation {
    def errorMessage: String = "Error during parsing DateTime"
  }
}
