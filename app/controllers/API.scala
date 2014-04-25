package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._

import reactivemongo.bson.BSONObjectID

import models.{ Hacker, Profile, Project }
import engine.Projects

object API extends Controller with OAuth2 {

  def createProject = Authenticated.async(parse.json) { implicit req =>
    implicit val reader = projectCreationReader(me)
    reader.reads(req.body).fold(
      err => Future.successful { BadRequest(Json.obj("error" -> "Bad request")) },
      project => Projects.insert(project).flatMap { project =>
        Projects.findHackersOf(project).map(_.map(h => (h.oid -> h)).toMap).map { hackers =>
          implicit val writer = projectWriter(hackers)
          Ok(Json.toJson(project))
        }
      }
    )
  }

  // UTILS

  def projectCreationReader(leader: Hacker): Reads[Project] = {
    (
      (__ \ "name").read[String]
    ).map { case name =>
      Project.create(name, leader)
    }
  }

  def projectWriter(hackers: Map[BSONObjectID, Hacker]) = new Writes[Project] {
    def writes(project: Project) = Json.obj(
      "name" -> project.name,
      "leader" -> hackers.get(project.leaderId),
      "team" -> JsArray(project.team.flatMap(hackers.get(_).map(Json.toJson(_))))
    )
  }

  implicit val hackerWriter = new Writes[Hacker] {
    implicit val profileWriter = Json.writes[Profile]
    def writes(hacker: Hacker) = Json.obj(
      "id" -> hacker.id,
      "email" -> hacker.email,
      "name" -> hacker.name,
      "profile" -> hacker.profile
    )
  }

}
