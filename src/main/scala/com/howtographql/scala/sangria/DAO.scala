package com.howtographql.scala.sangria

import slick.jdbc.H2Profile.api._
import DBSchema._
import scala.concurrent.Future
import com.howtographql.scala.sangria.models.{Link, User, Vote}
import sangria.execution.deferred.RelationIds
import sangria.execution.deferred.SimpleRelation

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

  def getVotesByUserIds(ids: Seq[Int]): Future[Seq[Vote]] = {
    println(s"DAO.getVotesByUserIds [${ids.mkString(", ")}]")
    db.run(Votes.filter(_.userId inSet ids).result)
  }

  def getVotesByRelationIds(rel: RelationIds[Vote]): Future[Seq[Vote]] = {
    println(s"DAO.getVotesByRelationIds [${rel.rawIds.mkString(", ")}]")
    db.run(Votes.filter({ vote =>
      rel.rawIds.collect({
        case (SimpleRelation("byUser"), ids: Seq[Int]) => vote.userId inSet ids
        case (SimpleRelation("byLink"), ids: Seq[Int]) => vote.linkId inSet ids
      }).foldLeft(true: Rep[Boolean])(_ || _)
    }).result)
  }
}
