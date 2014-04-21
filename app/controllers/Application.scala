package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._

import engine.{ Hackers, Projects }

object Application extends Controller with OAuth2 {

  def index = Authenticated.async { implicit req =>
    for {
      hackers <- Hackers.findAll
      (projects, teammates) <- Projects.findAllWithHackers
    } yield Ok(views.html.index(hackers, projects, teammates))
  }

}
