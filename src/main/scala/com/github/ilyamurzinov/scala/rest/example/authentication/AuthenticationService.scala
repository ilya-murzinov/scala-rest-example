package com.github.ilyamurzinov.scala.rest.example.authentication

import java.util.UUID

import com.github.ilyamurzinov.scala.rest.example.domain.User
import spray.http.{HttpCookie, StatusCodes}
import spray.routing._
import spray.routing.directives.BasicDirectives

import scala.collection.mutable

trait AuthenticationService extends BasicDirectives {
  val tokens: mutable.MutableList[String] = mutable.MutableList()

  protected def validateToken(accessToken: Option[HttpCookie]): Directive1[Boolean] = {
    accessToken match {
      case None =>
        provide(false)
      case Some(cookie) =>
        provide(tokens.contains(cookie.content))
    }
  }

  protected def acquireToken(user: User): Option[Token] = {
    if (user.username == "test" && user.password == "test") {
      val string: String = UUID.randomUUID().toString
      tokens += string
      Option(new Token(string))
    } else {
      Option.empty
    }
  }
}

case class Token(token: String)
