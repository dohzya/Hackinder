package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._

import engine.Hackers

object Application extends Controller with OAuth2 {

  def index = Authenticated.async { implicit req =>
    Hackers.findAll.map { hackers =>
      Ok(views.html.index(hackers))
    }
  }

}
