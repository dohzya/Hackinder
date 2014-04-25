package controllers

import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._

import reactivemongo.bson.BSONObjectID

import engine.{ Projects, Events, Hackers, Notifications }
import models.{ Event, Hacker, Profile, Project, Notification }
import models.{ AskProjectNotification, InviteHackerNotification, ParticipationNotification }

object API extends Controller with Context {

  def createProject = WithContext.async(parse.json) { implicit req =>
    implicit val reader = projectCreationReader(me)
    reader.reads(req.body).fold(
      err => Future.successful { BadRequest(Json.obj("error" -> "Bad request")) },
      project => for {
        insert <- Projects.insert(project)
        _ <- Future.sequence(currentEvent.map( e => Events.addProject(e, insert)).toSeq)
        json <- Projects.findHackersOf(insert).map(_.map(h => (h.oid -> h)).toMap).map { hackers =>
          implicit val writer = projectWriter(hackers)
          Json.toJson(insert)
        }
      } yield Ok(json)
    )
  }

  def createEvent = WithContext.async(parse.json) { implicit req =>
    implicit val reader = eventCreationReader
    reader.reads(req.body).fold(
      err => Future.successful { BadRequest(Json.obj("error" -> "Bad request")) },
      event => Events.insert(event).flatMap { event =>
        Events.getProjectsAndHackers(event).map { case (projects, hackers) =>
          implicit val writer = eventWriter(projects, hackers)
          Ok(Json.toJson(event))
        }
      }
    )
  }

  def notifications = WithContext.async { implicit req =>
    Notifications.ofUser.map { case (notifications, projects, hackers) =>
      implicit val writer = notificationsWriter(projects, hackers)
      Ok(Json.toJson(notifications))
    }
  }

  // Receive hacker email on POST
  def invite = WithContext.async(parse.json) { implicit req =>
    req.body.validate[String] match {
      case JsSuccess(email, _) => for {
        project <- Projects.ofZentrepreneur(me)
        hacker <- Hackers.findByEmail(email)
        notif <- Future.successful(for {
          p <- project
          h <- hacker
          invite = InviteHackerNotification.create(p, h.oid)
        } yield invite)
        insert <- Future.sequence(notif.map(Notifications.insert(_)).toSeq)
      } yield Ok
      case JsError(e) => Future.successful(BadRequest)
    }
  }

  // UTILS

  def eventCreationReader: Reads[Event] = {
    (
      (__ \ "name").read[String] and
      (__ \ "date").read[DateTime]
    )(
      (name: String, date: DateTime) => Event.create(name, date)
    )
  }

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

  def eventWriter(projects: Map[BSONObjectID, Project], hackers: Map[BSONObjectID, Hacker]) = new Writes[Event] {
    def writes(event: Event) = Json.obj(
      "name" -> event.name,
      "date" -> event.date,
      "hackers" -> JsArray(event.hackers.flatMap(hackers.get(_).map(Json.toJson(_)))),
      "projects" -> JsArray(event.projects.flatMap {
        projects.get(_).map { project =>
          implicit val writer = projectWriter(hackers)
          Json.toJson(project)
        }
      })
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

  def notificationsWriter(projects: Map[BSONObjectID, Project], hackers: Map[BSONObjectID, Hacker]) = new Writes[Notification] {
    implicit val _projectWriter = projectWriter(hackers)
    implicit val participationHandler = new Writes[ParticipationNotification] {
      def writes(notif: ParticipationNotification) = Json.obj(
        "id" -> notif.id,
        "type" -> notif.typ
      )
    }
    implicit val inviteHackerHandler = new Writes[InviteHackerNotification] {
      def writes(notif: InviteHackerNotification) = Json.obj(
        "id" -> notif.id,
        "project" -> projects.get(notif.projectId),
        "type" -> notif.typ
      )
    }
    implicit val askProjectHandler = new Writes[AskProjectNotification] {
      def writes(notif: AskProjectNotification) = Json.obj(
        "id" -> notif.id,
        "hacker" -> hackers.get(notif.hackerId),
        "type" -> notif.typ
      )
    }

    def writes(notification: Notification) = notification match {
      case notif: ParticipationNotification => participationHandler.writes(notif)
      case notif: InviteHackerNotification => inviteHackerHandler.writes(notif)
      case notif: AskProjectNotification => askProjectHandler.writes(notif)
    }
  }

}
