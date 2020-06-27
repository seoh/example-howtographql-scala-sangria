package com.howtographql.scala.sangria

import sangria.schema.ObjectType
import sangria.schema.Field
import sangria.schema.ListType

import sangria.schema.{IntType, StringType, Schema, fields}
import sangria.macros.derive._
import models._
import sangria.schema.OptionType
import sangria.schema.Argument
import sangria.schema.ListInputType
import sangria.execution.deferred.Fetcher
import sangria.execution.deferred.HasId
import sangria.execution.deferred.DeferredResolver
import sangria.schema.ScalarType
import akka.http.scaladsl.model.DateTime
import sangria.ast.StringValue
import sangria.execution.deferred.Relation
import sangria.execution.deferred.RelationIds
import shapeless.ops.record.Fields
import sangria.schema.InputObjectType


object GraphQLSchema {

  implicit val GraphQLDateTime = ScalarType[DateTime](
    "DateTime",
    coerceOutput = (dt, _) => dt.toString,
    coerceInput = {
      case StringValue(dt, _, _) => DateTime.fromIsoDateTimeString(dt).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    },
    coerceUserInput = {
      case s: String => DateTime.fromIsoDateTimeString(s).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    }
  )


  // val IdentifiableType = InterfaceType(
  //   "Identifiable",
  //   fields[Unit, Identifiable](
  //     Field("id", IntType, resolve = _.value.id)
  //   )
  // )

  // implicit val LinkType = deriveObjectType[Unit, Link](
  //     Interfaces(IdentifiableType)
  // )

  // // implicit val LinkType = ObjectType[Unit, Link](
  //   "Link",
  //   fields[Unit, Link](
  //     Field("id", IntType, resolve = _.value.id),
  //     Field("url", StringType, resolve = _.value.url),
  //     Field("description", StringType, resolve = _.value.description),
  //     Field("createdAt", GraphQLDateTime, resolve = _.value.createdAt)
  //   )
  // )
  val LinkType = deriveObjectType[Unit, Link](
    ReplaceField("postedBy",
      Field("postedBy", UserType, resolve = c => usersFetcher.defer(c.value.postedBy))),
    AddFields(
      Field("votes", ListType(VoteType), resolve = c => votesFetcher.deferRelSeq(voteByLinkRel, c.value.id))
    )
  )

  val linkByUserRel = Relation[Link, Int]("byUser", link => Seq(link.postedBy))
  val linksFetcher = Fetcher.rel(
    (ctx: MyContext, ids: Seq[Int]) => ctx.dao.getLinks(ids),
    (ctx: MyContext, ids: RelationIds[Link]) => ctx.dao.getLinksByUserIds(ids(linkByUserRel))
  )


  lazy val UserType: ObjectType[Unit, User] = deriveObjectType[Unit, User](
    AddFields(
      Field("links", ListType(LinkType), resolve = c => linksFetcher.deferRelSeq(linkByUserRel, c.value.id)),
      Field("votes", ListType(VoteType), resolve = c => votesFetcher.deferRelSeq(voteByUserRel, c.value.id))
    )
  )
  val usersFetcher = Fetcher(
    (ctx: MyContext, ids: Seq[Int]) => ctx.dao.getUsers(ids)
  )

  lazy val VoteType: ObjectType[Unit, Vote] = deriveObjectType[Unit, Vote](
    ExcludeFields("userId", "linkId"),
    AddFields(
      Field("user", UserType, resolve = c => usersFetcher.defer(c.value.userId)),
      Field("link", LinkType, resolve = c => linksFetcher.defer(c.value.linkId))
    )
  )
  val voteByUserRel = Relation[Vote, Int]("byUser", v => Seq(v.userId))
  val voteByLinkRel = Relation[Vote, Int]("byLink", v => Seq(v.linkId))
  val votesFetcher = Fetcher.rel(
    (ctx: MyContext, ids: Seq[Int]) => ctx.dao.getVotes(ids),
    (ctx: MyContext, ids: RelationIds[Vote]) => ctx.dao.getVotesByRelationIds(ids)
  )


  val Resolver = DeferredResolver.fetchers(linksFetcher, usersFetcher, votesFetcher)


  val Id = Argument("id", IntType)
  val Ids = Argument("ids", ListInputType(IntType))

  val QueryType = ObjectType(
    "Query",
    fields[MyContext, Unit](
      Field("allLinks", ListType(LinkType), resolve = c => c.ctx.dao.allLinks),
      Field(
        "link",
        OptionType(LinkType),
        arguments = Id :: Nil,
        resolve = c => linksFetcher.deferOpt(c.arg(Id))
      ),
      Field(
        "links",
        ListType(LinkType),
        arguments = Ids :: Nil,
        resolve = c => linksFetcher.deferSeq(c.arg(Ids))
      ),
      Field(
        "users",
        ListType(UserType),
        arguments = Ids :: Nil,
        resolve = c => usersFetcher.deferSeq(c.arg(Ids))
      ),
      Field(
        "votes",
        ListType(VoteType),
        arguments = Ids :: Nil,
        resolve = c => votesFetcher.deferSeq(c.arg(Ids))
      )
    )
  )


  implicit val AuthProviderEmailInputType: InputObjectType[AuthProviderEmail] = deriveInputObjectType[AuthProviderEmail](
    InputObjectTypeName("AUTH_PROVIDER_EMAIL")
  )

  lazy val AuthProviderSignupDataInputType: InputObjectType[AuthProviderSignupData] = deriveInputObjectType[AuthProviderSignupData]()


  val SchemaDefinition = Schema(QueryType)
}
