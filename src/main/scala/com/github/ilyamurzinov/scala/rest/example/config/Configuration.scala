package com.github.ilyamurzinov.scala.rest.example.config

import com.typesafe.config.ConfigFactory
import util.Try

trait Configuration {

  val config = ConfigFactory.load()

  lazy val dbLocation = Try(config.getString("db.location")).getOrElse("")

  lazy val dbName = Try(config.getString("db.name")).getOrElse("rest")

  lazy val dbUser = Try(config.getString("db.user")).toOption.orNull

  lazy val dbPassword = Try(config.getString("db.password")).toOption.orNull
}
