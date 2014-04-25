package engine

import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.joda.time.DateTime

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._

import play.api.Play.current

import models.{ Event, Project, Hacker }

object Events {
  def db = ReactiveMongoPlugin.db

  def collection = db("events")(BSONCollectionProducer)

  def isEmpty: Future[Boolean] = {
    collection.find(BSONDocument()).one[BSONDocument].map(_.isEmpty)
  }

  def insert(event: Event): Future[Event] = for {
    _ <- collection.insert(event)
  } yield event

  def findAll: Future[Seq[Event]] = {
    collection.find(BSONDocument()).cursor[Event].collect[Seq]()
  }

  def findAllById(ids: Seq[BSONObjectID]): Future[Seq[Event]] = {
    collection.find(BSONDocument("_id" -> BSONDocument("$in" -> ids)))
      .cursor[Event]
      .collect[Seq]()
  }

  def findCurrentEvent(): Future[Option[Event]] = {
    collection
      .find(BSONDocument())
      .sort(BSONDocument("date" -> -1))
      .one[Event].map(_.flatMap { event =>
        if (event.date.getMillis > DateTime.now.getMillis) Some(event)
        else None
      })
  }

  def getProjectsAndHackers(event: Event): Future[(Map[BSONObjectID, (Project, Map[BSONObjectID, Hacker])], Map[BSONObjectID, Hacker])] = for {
    projectsWithHackers <- Projects.findAllByIdWithHackers(event.projects)
    projectsWithHackersMap = projectsWithHackers.map { case (p, hackers) => (p.oid, (p, hackers)) }.toMap
    hackers <- Hackers.findAllById(event.hackers)
    hackersMap = hackers.map(h => (h.oid, h)).toMap
  } yield (projectsWithHackersMap, hackersMap)

  def findByName(name: String): Future[Option[Event]] = {
    collection.find(BSONDocument("name" -> name)).one[Event]
  }

  def addProject(event: Event, project: Project): Future[Event] = {
    collection.update(
      BSONDocument("_id" -> event.oid),
      BSONDocument("$addToSet" -> BSONDocument("projects" -> project.oid))
    ).flatMap { _ =>
      collection.find(
        BSONDocument("_id" -> event.oid),
        BSONDocument("projects" -> 1)
      ).one[BSONDocument].map { doc =>
        val projects = doc.flatMap(_.getAs[Seq[BSONObjectID]]("projects")).getOrElse {
          throw new java.lang.RuntimeException(s"Can't find event ${event.oid}'s 'projects' field!")
        }
        event.copy(projects = projects)
      }
    }
  }

  implicit val eventHandler = Macros.handler[Event]

}
