package engine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._

import play.api.Play.current

import models.{ Hacker, Profile }
import models.Notification
import models.{ AskProjectNotification, InviteHackerNotification, ParticipationNotification }

object Notifications {

  trait BSONDocumentHandler[T] extends BSONDocumentReader[T] with BSONDocumentWriter[T]

  def db = ReactiveMongoPlugin.db

  def collection = db("notifications")(BSONCollectionProducer)

  def insert(notification: Notification): Future[Notification] = {
    for {
      _ <- collection.insert(notification)
    } yield notification
  }

  def findById(id: BSONObjectID): Future[Option[Notification]] = {
    collection.find(BSONDocument("_id" -> id)).one[Notification]
  }

  def findAllByHackerId(hackerId: BSONObjectID): Future[Option[Notification]] = {
    collection.find(BSONDocument("hackerId" -> hackerId)).one[Notification]
  }

  def findAllByProjectId(projectId: BSONObjectID): Future[Option[Notification]] = {
    collection.find(BSONDocument("projectId" -> projectId)).one[Notification]
  }

  implicit val participationHandler = new BSONDocumentHandler[ParticipationNotification] {
    def write(document: ParticipationNotification) = BSONDocument(
      "oid" -> document.oid,
      "eventId" -> document.eventId,
      "hackerId" -> document.hackerId,
      "typ" -> document.typ
    )
    def read(buffer: BSONDocument) = ParticipationNotification(
      oid = buffer.getAs[BSONObjectID]("oid").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field oid: ${BSONDocument.pretty(buffer)}")
      },
      eventId = buffer.getAs[BSONObjectID]("eventId").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field eventId: ${BSONDocument.pretty(buffer)}")
      },
      hackerId = buffer.getAs[BSONObjectID]("hackerId").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field hackerId: ${BSONDocument.pretty(buffer)}")
      }
    )
  }
  implicit val inviteHackerHandler = new BSONDocumentHandler[InviteHackerNotification] {
    def write(document: InviteHackerNotification) = BSONDocument(
      "oid" -> document.oid,
      "projectId" -> document.projectId,
      "hackerId" -> document.hackerId,
      "typ" -> document.typ
    )
    def read(buffer: BSONDocument) = InviteHackerNotification(
      oid = buffer.getAs[BSONObjectID]("oid").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field oid: ${BSONDocument.pretty(buffer)}")
      },
      projectId = buffer.getAs[BSONObjectID]("projectId").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field projectId: ${BSONDocument.pretty(buffer)}")
      },
      hackerId = buffer.getAs[BSONObjectID]("hackerId").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field hackerId: ${BSONDocument.pretty(buffer)}")
      }
    )
  }
  implicit val askProjectHandler = new BSONDocumentHandler[AskProjectNotification] {
    def write(document: AskProjectNotification) = BSONDocument(
      "oid" -> document.oid,
      "hackerId" -> document.hackerId,
      "projectId" -> document.projectId,
      "typ" -> document.typ
    )
    def read(buffer: BSONDocument) = AskProjectNotification(
      oid = buffer.getAs[BSONObjectID]("oid").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field oid: ${BSONDocument.pretty(buffer)}")
      },
      hackerId = buffer.getAs[BSONObjectID]("hackerId").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field hackerId: ${BSONDocument.pretty(buffer)}")
      },
      projectId = buffer.getAs[BSONObjectID]("projectId").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field projectId: ${BSONDocument.pretty(buffer)}")
      }
    )
  }

  implicit val notificationHandler = new BSONDocumentHandler[Notification] {
    def write(document: Notification) = document match {
      case doc: ParticipationNotification => participationHandler.write(doc)
      case doc: InviteHackerNotification => inviteHackerHandler.write(doc)
      case doc: AskProjectNotification => askProjectHandler.write(doc)
    }
    def read(buffer: BSONDocument): Notification = buffer.getAs[String]("typ") match {
      case Some(ParticipationNotification.typ) => participationHandler.read(buffer)
      case Some(InviteHackerNotification.typ) => inviteHackerHandler.read(buffer)
      case Some(AskProjectNotification.typ) => askProjectHandler.read(buffer)
      case _ => throw new java.lang.RuntimeException(s"Bad typ: ${BSONDocument.pretty(buffer)}")
    }
  }

}
