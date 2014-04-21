package engine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._

import play.api.Play.current

import models.{ Hacker, Project }

object Projects {

  def db = ReactiveMongoPlugin.db

  def collection = db("projects")(BSONCollectionProducer)

  def isEmpty: Future[Boolean] = {
    collection.find(BSONDocument()).one[BSONDocument].map(_.isEmpty)
  }

  def insert(project: Project): Future[Project] = {
    for {
      _ <- collection.insert(project)
    } yield project
  }

  def findAll(): Future[Seq[Project]] = {
    collection.find(BSONDocument()).cursor[Project].collect[Seq]()
  }

  def findAllWithHackers(): Future[(Seq[Project], Map[BSONObjectID, Hacker])] = {
    for {
      projects <- findAll
      hackers <- Hackers.findAllById(projects.flatMap(_.team))
      hackersMap = hackers.map(h => (h.oid, h)).toMap
    } yield (projects, hackersMap)
  }

  def findAllById(ids: Seq[BSONObjectID]): Future[Seq[Project]] = {
    collection.find(BSONDocument("_id" -> BSONDocument("$in" -> ids)))
              .cursor[Project]
              .collect[Seq]()
  }

  def findByName(name: String): Future[Option[Project]] = {
    collection.find(BSONDocument("name" -> name)).one[Project]
  }

  implicit val projectHandler = Macros.handler[Project]

}
