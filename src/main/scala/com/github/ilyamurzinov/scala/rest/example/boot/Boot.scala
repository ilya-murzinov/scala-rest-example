package com.github.ilyamurzinov.scala.rest.example.boot

import akka.actor.{Props, ActorSystem}
import spray.servlet.WebBoot
import com.github.ilyamurzinov.scala.rest.example.rest.RestServiceActor

class Boot extends WebBoot {

  implicit val system = ActorSystem("rest-service-example")

  val serviceActor = system.actorOf(Props[RestServiceActor], "rest-endpoint")
}