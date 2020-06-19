package com.howtographql.scala.sangria

import slick.jdbc.H2Profile.api._
import DBSchema._
import scala.concurrent.Future
import com.howtographql.scala.sangria.models.Link

class DAO(db: Database) {
  def allLinks = db.run(Links.result)

  def getLink(id: Int): Future[Option[Link]] =
    db.run(Links.filter(_.id === id).result.headOption)

  def getLinks(ids: Seq[Int]): Future[Seq[Link]] =
    db.run(Links.filter(_.id inSet ids).result)
}
