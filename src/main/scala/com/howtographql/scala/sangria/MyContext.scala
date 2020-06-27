package com.howtographql.scala.sangria

import com.howtographql.scala.sangria.models._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class MyContext(dao: DAO, currentUser: Option[User] = None) {

  def login(email: String, password: String): Future[User] = {
    println(s"MyContext.login $email, $password")

    dao.authenticate(email, password) map {
      case Some(user) => user
      case None => throw AuthenticationException("email or password are incorrect")
    }
  }

  def ensureAuthenticated() =
    if(currentUser.isEmpty) throw AuthorizationException("You do not have permission. Please sign in.")
}
