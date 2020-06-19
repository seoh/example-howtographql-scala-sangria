package com.howtographql.scala.sangria

import sangria.schema.ObjectType
import sangria.schema.Field
import sangria.schema.ListType

import sangria.schema.{IntType, StringType, Schema, fields}
import sangria.macros.derive._
import models.Link
import sangria.schema.OptionType
import sangria.schema.Argument
import sangria.schema.ListInputType


object GraphQLSchema {

  val LinkType = ObjectType[Unit, Link](
    "Link",
    fields[Unit, Link](
      Field("id", IntType, resolve = _.value.id),
      Field("url", StringType, resolve = _.value.url),
      Field("description", StringType, resolve = _.value.description)
    )
  )

  val QueryType = ObjectType(
    "Query",
    fields[MyContext, Unit](
      Field("allLinks", ListType(LinkType), resolve = c => c.ctx.dao.allLinks),
      Field(
        "link",
        OptionType(LinkType),
        arguments = List(Argument("id", IntType)),
        resolve = c => c.ctx.dao.getLink(c.arg[Int]("id"))
      ),
      Field(
        "links",
        ListType(LinkType),
        arguments = List(Argument("ids", ListInputType(IntType))),
        resolve = c => c.ctx.dao.getLinks(c.arg[Seq[Int]]("ids"))
      )
    )
  )

  val SchemaDefinition = Schema(QueryType)
}
