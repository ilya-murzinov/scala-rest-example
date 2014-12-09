package com.github.ilyamurzinov.scala.rest.example.rest

import java.text.{ParseException, SimpleDateFormat}
import java.util.Date

import akka.actor.{Actor, ActorContext}
import akka.event.slf4j.SLF4JLogging
import com.github.ilyamurzinov.scala.rest.example.authentication.AuthenticationService
import com.github.ilyamurzinov.scala.rest.example.dao.CustomerDAO
import com.github.ilyamurzinov.scala.rest.example.domain._
import net.liftweb.json.Serialization._
import net.liftweb.json.{DateFormat, Formats}
import spray.http._
import spray.httpx.unmarshalling._

import spray.routing._

class RestServiceActor extends Actor with RestService {

  implicit def actorRefFactory: ActorContext = context

  def receive = runRoute(rest ~ secureRest ~ static)
}

trait RestService extends HttpService with SLF4JLogging with AuthenticationService {

  val ACCESS_TOKEN_COOKIE: String = "access_token"

  val customerService = new CustomerDAO

  implicit val executionContext = actorRefFactory.dispatcher

  implicit val liftJsonFormats = new Formats {
    val dateFormat = new DateFormat {
      val sdf = new SimpleDateFormat("yyyy-MM-dd")

      def parse(s: String): Option[Date] = try {
        Some(sdf.parse(s))
      }
      catch {
        case e: Exception => None
      }

      def format(d: Date): String = sdf.format(d)
    }
  }

  implicit val string2Date = new FromStringDeserializer[Date] {
    def apply(value: String) = {
      val sdf = new SimpleDateFormat("yyyy-MM-dd")
      try Right(sdf.parse(value))
      catch {
        case e: ParseException =>
          Left(MalformedContent("'%s' is not a valid Date value" format value, e))
      }
    }
  }

  implicit val customRejectionHandler = RejectionHandler {
    case rejections => mapHttpResponse {
      response =>
        response.withEntity(HttpEntity(ContentType(MediaTypes.`application/json`),
          write(Map("error" -> response.entity.asString))))
    } {
      RejectionHandler.Default(rejections)
    }
  }

  implicit val umu = new FromRequestUnmarshaller[User] {
    def apply(httpRequest: HttpRequest) = {
      Unmarshaller(MediaTypes.`application/json`) {
        case httpEntity: HttpEntity =>
          read[User](httpEntity.asString(HttpCharsets.`UTF-8`))
      }.apply(httpRequest.entity)
    }
  }

  implicit val umc = new FromRequestUnmarshaller[Customer] {
    def apply(httpRequest: HttpRequest) = {
      Unmarshaller(MediaTypes.`application/json`) {
        case httpEntity: HttpEntity =>
          read[Customer](httpEntity.asString(HttpCharsets.`UTF-8`))
      }.apply(httpRequest.entity)
    }
  }

  val static =
    path("") {
      respondWithMediaType(MediaTypes.`text/html`) {
        get {
          getFromResource("site/index.html")
        }
      }
    } ~ pathPrefix("fragments") {
      respondWithMediaType(MediaTypes.`application/javascript`) {
        get {
          getFromResourceDirectory("site/fragments")
        }
      }
    } ~ pathPrefix("js") {
      respondWithMediaType(MediaTypes.`application/javascript`) {
        get {
          getFromResourceDirectory("site/js")
        }
      }
    } ~
      pathPrefix("css") {
        respondWithMediaType(MediaTypes.`text/css`) {
          get {
            getFromResourceDirectory("site/css")
          }
        }
      }

  val rest =
    pathPrefix("api") {
      path("") {
        respondWithMediaType(MediaTypes.`text/html`) {
          get {
            getFromResource("site/api-description.html")
          }
        }
      } ~
        respondWithMediaType(MediaTypes.`application/json`) {
          path("tokens") {
            post {
              entity(as[User]) {
                user: User =>
                  acquireToken(user) match {
                    case None =>
                      respondWithStatus(StatusCodes.Unauthorized) {
                        complete("")
                      }
                    case Some(token) =>
                      setCookie(HttpCookie(ACCESS_TOKEN_COOKIE, token.token)) {
                        complete(write(token))
                      }
                  }
              }
            }
          }
        }
    }

  val secureRest =
    respondWithMediaType(MediaTypes.`application/json`) {
      optionalCookie(ACCESS_TOKEN_COOKIE) { accessToken => {
        pathPrefix("api") {
            validateToken(accessToken) {
              case false =>
                respondWithStatus(StatusCodes.Unauthorized) {
                  complete("")
                }
              case true =>
                path("customer") {
                  post {
                    entity(as[Customer]) {
                      customer: Customer =>
                        ctx: RequestContext =>
                          handleRequest(ctx, StatusCodes.Created) {
                            log.debug("Creating customer: %s".format(customer))
                            customerService.create(customer)
                          }
                    }
                  }
                } ~
                  get {
                    parameters('firstName.as[String] ?, 'lastName.as[String] ?, 'birthday.as[Date] ?).as(CustomerSearchParameters) {
                      searchParameters: CustomerSearchParameters => {
                        ctx: RequestContext =>
                          handleRequest(ctx) {
                            log.debug("Searching for customers with parameters: %s".format(searchParameters))
                            customerService.search(searchParameters)
                          }
                      }
                    }
                  } ~
                  path("customer" / LongNumber) {
                    customerId =>
                      put {
                        entity(as[Customer]) {
                          customer: Customer =>
                            ctx: RequestContext =>
                              handleRequest(ctx) {
                                log.debug("Updating customer with id %d: %s".format(customerId, customer))
                                customerService.update(customerId, customer)
                              }
                        }
                      } ~
                        delete {
                          ctx: RequestContext =>
                            handleRequest(ctx) {
                              log.debug("Deleting customer with id %d".format(customerId))
                              customerService.delete(customerId)
                            }
                        } ~
                        get {
                          ctx: RequestContext =>
                            handleRequest(ctx) {
                              log.debug("Retrieving customer with id %d".format(customerId))
                              customerService.get(customerId)
                            }
                        }
                  }
            }
          }
        }
      }
    }

  protected def handleRequest(ctx: RequestContext, successCode: StatusCode = StatusCodes.OK)(action: => Either[Failure, _]) {
    action match {
      case Right(result: Object) =>
        ctx.complete(successCode, write(result))
      case Left(error: Failure) =>
        ctx.complete(error.getStatusCode, net.liftweb.json.Serialization.write(Map("error" -> error.message)))
      case _ =>
        ctx.complete(StatusCodes.InternalServerError)
    }
  }
}