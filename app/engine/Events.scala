package engine

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._

import models.{ Event, Project }

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

  def findAllById(ids: Seq[BSONObjectID]): Future[Seq[Project]] = {
    collection.find(BSONDocument("_id" -> BSONDocument("$in" -> ids)))
              .cursor[Event]
              .collect[Seq]()
  }

  def findAllWithProjects: Future[(Seq[Event], Map[BSONObjectId, Project])] = for {
    events <- findAll
    projects <- Projects.findAllById(events.flatMap(_.projects))
    projectsMap = projects.map(p => (p.oid, p)).toMap
  } yield (events, projectsMap)

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
        val projects = doc.flatMap(_.getAs[Seq[BSONObjectId]]("projects")).getOrElse {
          throw new java.lang.RuntimeException(s"Can't find event ${event.oid}'s 'projects' field!")
        }
        event.copy(projects = projects)
      }
    }
  }

  implicit val eventHandler = Macros.handler[Event]

}
