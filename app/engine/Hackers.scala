package engine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._

import play.api.Play.current

import models.{ Profile, Hacker }

object Hackers {

  def db = ReactiveMongoPlugin.db

  def collection = db("hackers")(BSONCollectionProducer)

  def isEmpty: Future[Boolean] = {
    collection.find(BSONDocument()).one[BSONDocument].map(_.isEmpty)
  }

  private def insert(hacker: Hacker): Future[Hacker] = {
    for {
      _ <- collection.insert(hacker)
    } yield hacker
  }

  def findAll(): Future[Seq[Hacker]] = {
    collection.find(BSONDocument()).cursor[Hacker].collect[Seq]()
  }

  def findAllById(ids: Seq[BSONObjectID]): Future[Seq[Hacker]] = {
    collection.find(BSONDocument("_id" -> BSONDocument("$in" -> ids)))
              .cursor[Hacker]
              .collect[Seq]()
  }

  def findById(id: BSONObjectID): Future[Option[Hacker]] = {
    collection.find(BSONDocument("_id" -> id)).one[Hacker]
  }

  def findByEmail(email: String): Future[Option[Hacker]] = {
    collection.find(BSONDocument("profile.email" -> email)).one[Hacker]
  }

  def fromProfile(profile: Profile): Future[Hacker] = {
    collection.find(BSONDocument("profile.id" -> profile.id)).one[Hacker].flatMap {
      case Some(hacker) => updateProfile(hacker, profile)
      case None => insert(Hacker.create(profile))
    }
  }

  private def updateProfile(hacker: Hacker, profile: Profile): Future[Hacker] = {
    collection.update(
      BSONDocument("_id" -> hacker.oid),
      BSONDocument("$set" -> BSONDocument("profile" -> profile))
    ).map(_ => hacker.copy(profile = profile))
  }

  implicit val profileHandler = Macros.handler[Profile]
  implicit val hackerHandler = Macros.handler[Hacker]

}
