package com.howtographql.scala.sangria

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import spray.json.{JsObject, JsString, JsValue}

import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure}

import sangria.execution._
import sangria.ast.Document
import sangria.execution.ErrorWithResolver
import sangria.execution.QueryAnalysisError
import sangria.parser.QueryParser
import sangria.marshalling.sprayJson._

import models.{AuthenticationException, AuthorizationException}


object GraphQLServer {
  private val dao = DBSchema.createDatabase

  def endpoint(requestJSON: JsValue)(implicit ec: ExecutionContext): Route = {

    val JsObject(fields) = requestJSON
    val JsString(query) = fields("query")

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        val operation = fields.get("operationName") collect {
          case JsString(op) => op
        }

        val variables = fields.get("variables") match {
          case Some(obj: JsObject) => obj
          case _ => JsObject.empty
        }

        complete(executeGraphQLQuery(queryAst, operation, variables))
      case Failure(error) =>
        complete(BadRequest, JsObject("error" -> JsString(error.getMessage)))
    }
  }

  val errorHandler = ExceptionHandler {
    case (_, AuthenticationException(message)) => HandledException(message)
    case (_, AuthorizationException(message)) => HandledException(message)
  }

  private def executeGraphQLQuery(query: Document, operation: Option[String], vars: JsObject)(implicit ec: ExecutionContext) = {
    Executor.execute(
      GraphQLSchema.SchemaDefinition,
      query,
      MyContext(dao),
      variables = vars,
      operationName = operation,
      deferredResolver = GraphQLSchema.Resolver,
      exceptionHandler = errorHandler,
      middleware = AuthMiddle :: Nil
    ).map(OK -> _)
      .recover {
      case error: QueryAnalysisError => BadRequest -> error.resolveError
      case error: ErrorWithResolver => InternalServerError -> error.resolveError
    }
  }
}
