package com.howtographql.scala.sangria

import slick.jdbc.H2Profile.api._
import DBSchema._
import scala.concurrent.Future
import com.howtographql.scala.sangria.models.Link

class DAO(db: Database) {
  def allLinks = db.run(Links.result)

  def getLinks(ids: Seq[Int]): Future[Seq[Link]] = {
    println(s"DAO.getLinks [${ids.mkString(", ")}]")
    db.run(Links.filter(_.id inSet ids).result)
  }
}
