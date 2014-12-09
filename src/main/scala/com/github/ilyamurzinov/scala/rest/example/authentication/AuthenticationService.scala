package com.github.ilyamurzinov.scala.rest.example.authentication

import java.util.UUID

import com.github.ilyamurzinov.scala.rest.example.domain.User
import spray.http.{HttpCookie, StatusCodes}
import spray.routing._

import scala.collection.mutable

trait AuthenticationService {
  val tokens: mutable.MutableList[String] = mutable.MutableList()

  protected def validateToken(ctx: RequestContext, accessToken: Option[HttpCookie])(route: Route) {
    accessToken match {
      case None =>
        ctx.complete(StatusCodes.Unauthorized)
      case Some(cookie) =>
        if (tokens.contains(cookie.content)) {
          route.apply(ctx)
        }
        else {
          ctx.complete(StatusCodes.Unauthorized)
        }
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
