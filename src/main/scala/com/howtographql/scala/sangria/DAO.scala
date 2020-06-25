package com.howtographql.scala.sangria

import slick.jdbc.H2Profile.api._
import DBSchema._
import scala.concurrent.Future
import com.howtographql.scala.sangria.models.{Link, User, Vote}

class DAO(db: Database) {
  def allLinks = db.run(Links.result)

  def getLinks(ids: Seq[Int]): Future[Seq[Link]] = {
    println(s"DAO.getLinks [${ids.mkString(", ")}]")
    db.run(Links.filter(_.id inSet ids).result)
  }

  def getUsers(ids: Seq[Int]): Future[Seq[User]] = {
    println(s"DAO.getUsers [${ids.mkString(", ")}]")
    db.run(Users.filter(_.id inSet ids).result)
  }

  def getVotes(ids: Seq[Int]): Future[Seq[Vote]] = {
    println(s"DAO.getVotes [${ids.mkString(", ")}]")
    db.run(Votes.filter(_.id inSet ids).result)
  }

  def getLinksByUserIds(ids: Seq[Int]): Future[Seq[Link]] = {
    println(s"DAO.getLinksByUserIds [${ids.mkString(", ")}]")
    db.run(Links.filter(_.postedBy inSet ids).result)
  }
}
