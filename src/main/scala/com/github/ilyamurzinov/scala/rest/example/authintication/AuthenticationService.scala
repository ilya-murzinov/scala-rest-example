package com.github.ilyamurzinov.scala.rest.example.authintication

import java.util.UUID

import com.github.ilyamurzinov.scala.rest.example.domain.{User, FailureType, Failure}
import spray.http.StatusCodes
import spray.routing._

import scala.collection.mutable

trait AuthenticationService {
  val tokens: mutable.MutableList[String] = mutable.MutableList()

  protected def validateToken(ctx: RequestContext, accessToken: Option[String])(route: Route) {
    accessToken match {
      case None =>
        ctx.complete(StatusCodes.Unauthorized)
      case Some(str) =>
        if (tokens.contains(str)) {
          route.apply(ctx)
        } else {
          ctx.complete(StatusCodes.Unauthorized)
        }
    }
  }

  protected def acquireToken(user: User): Either[Failure, Token] = {
    try {
      checkUser(user)
      val string: String = UUID.randomUUID().toString
      tokens += string
      Right(new Token(string))
    } catch {
      case e: Exception =>
        Left(Failure("Wrong username or password", FailureType.Unauthorized))
    }
  }

  private def checkUser(user: User): Unit = {
    if (user.username != "test" || user.password != "test") {
      throw new Exception
    }
  }
}

case class Token(token: String)
