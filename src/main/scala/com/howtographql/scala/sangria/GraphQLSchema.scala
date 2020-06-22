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

  implicit val LinkType = deriveObjectType[Unit, Link]()

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
        resolve = c => c.ctx.dao.getLink(c.arg(Id))
      ),
      Field(
        "links",
        ListType(LinkType),
        arguments = Ids :: Nil,
        resolve = c => c.ctx.dao.getLinks(c.arg(Ids))
      )
    )
  )

  val SchemaDefinition = Schema(QueryType)
}
