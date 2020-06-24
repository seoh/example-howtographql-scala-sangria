package com.howtographql.scala.sangria

import akka.http.scaladsl.model.DateTime

package object models {
  case class Link(id: Int, url: String, description: String, createdAt: DateTime)
}
