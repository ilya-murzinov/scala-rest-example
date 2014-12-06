package com.github.ilyamurzinov.scala.rest.example.dao

import com.github.ilyamurzinov.scala.rest.example.config.Configuration
import com.github.ilyamurzinov.scala.rest.example.domain._
import java.sql._
import scala.Some
import scala.slick.driver.H2Driver.simple.Database.threadLocalSession
import scala.slick.driver.H2Driver.simple._
import slick.jdbc.meta.MTable

class CustomerDAO extends Configuration {

  private val db = Database.forURL(url = "jdbc:h2:~/%s/%s".format(dbLocation, dbName),
    user = dbUser, password = dbPassword, driver = "org.h2.Driver")

  db.withSession {
    if (MTable.getTables("customers").list().isEmpty) {
      Customers.ddl.create
    }
  }

  def create(customer: Customer): Either[Failure, Customer] = {
    try {
      val id = db.withSession {
        Customers returning Customers.id insert customer
      }
      Right(customer.copy(id = Some(id)))
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
    }

  def update(id: Long, customer: Customer): Either[Failure, Customer] = {
    try
      db.withSession {
        Customers.where(_.id === id) update customer.copy(id = Some(id)) match {
          case 0 => Left(notFoundError(id))
          case _ => Right(customer.copy(id = Some(id)))
        }
      }
    catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  def delete(id: Long): Either[Failure, Customer] = {
    try {
      db.withTransaction {
        val query = Customers.where(_.id === id)
        val customers = query.run.asInstanceOf[Vector[Customer]]
        customers.size match {
          case 0 =>
            Left(notFoundError(id))
          case _ => {
            query.delete
            Right(customers.head)
          }
        }
      }
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  def get(id: Long): Either[Failure, Customer] = {
    try {
      db.withSession {
        Customers.findById(id).firstOption match {
          case Some(customer: Customer) =>
            Right(customer)
          case _ =>
            Left(notFoundError(id))
        }
      }
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  def search(params: CustomerSearchParameters): Either[Failure, List[Customer]] = {
    implicit val typeMapper = Customers.dateTypeMapper

    try {
      db.withSession {
        val query = for {
          customer <- Customers if {
          Seq(
            params.firstName.map(customer.firstName is _),
            params.lastName.map(customer.lastName is _),
            params.birthday.map(customer.birthday is _)
          ).flatten match {
            case Nil => ConstColumn.TRUE
            case seq => seq.reduce(_ && _)
          }
        }
        } yield customer

        Right(query.run.toList)
      }
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  protected def databaseError(e: SQLException) =
    Failure("%d: %s".format(e.getErrorCode, e.getMessage), FailureType.DatabaseFailure)

  protected def notFoundError(customerId: Long) =
    Failure("Customer with id=%d does not exist".format(customerId), FailureType.NotFound)
}