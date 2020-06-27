package com.howtographql.scala.sangria

import sangria.execution.Middleware
import sangria.execution.MiddlewareBeforeField
import sangria.execution.MiddlewareQueryContext
import sangria.schema.{Action, Context}
import com.howtographql.scala.sangria.models.Authorized

object AuthMiddle extends Middleware[MyContext] with MiddlewareBeforeField[MyContext] {

  override type QueryVal = Unit
  override type FieldVal = Unit

  override def beforeQuery(context: MiddlewareQueryContext[MyContext, _, _]): Unit = ()

  override def afterQuery(queryVal: Unit, context: MiddlewareQueryContext[MyContext, _, _]): Unit = ()

  override def beforeField(queryVal: Unit, mctx: MiddlewareQueryContext[MyContext, _, _], ctx: Context[MyContext, _]): (Unit, Option[Action[MyContext, _]]) = {
    val requireAuth = ctx.field.tags contains Authorized

    if(requireAuth)
      ctx.ctx.ensureAuthenticated()

    continue
  }
}
