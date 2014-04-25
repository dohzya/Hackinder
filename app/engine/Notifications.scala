package engine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._

import play.api.Play.current

import controllers.Ctx
import models.{ Event, Hacker, Profile, Project }
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

  def createIfNeeded(event: Event, hackerId: BSONObjectID): Future[Unit] = {
    if (!event.hackers.contains(hackerId)) {
      collection.find(BSONDocument(
        "typ" -> ParticipationNotification.typ,
        "hackerId" -> hackerId,
        "eventId" -> event.oid
      )).one[BSONDocument].flatMap { notif =>
        if (notif.isEmpty) {
          Notifications.insert(ParticipationNotification.create(
            event = event,
            hackerId = hackerId
          )).map(_ => ())
        }
        else Future.successful { () }
      }
    }
    else Future.successful { () }
  }

  def findById(id: BSONObjectID): Future[Option[Notification]] = {
    collection.find(BSONDocument("_id" -> id)).one[Notification]
  }

  def checkForUser(id: BSONObjectID)(implicit ctx: Ctx): Future[Option[Notification]] = {
    collection.find(BSONDocument("_id" -> id, "hackerId" -> ctx.hacker.oid)).one[Notification]
  }

  def ofUser(implicit ctx: Ctx): Future[(Seq[Notification], Map[BSONObjectID, Project], Map[BSONObjectID, Hacker])] = {
    val query = ctx.event match {
      case Some(event) =>
        BSONDocument("$or" -> BSONArray(
          BSONDocument("hackerId" -> ctx.hacker.oid),
          BSONDocument(
            "leaderId" -> ctx.hacker.oid,
            "projectId" -> BSONDocument("$in" -> event.projects)
          )
        ))
      case None => BSONDocument("hackerId" -> ctx.hacker.oid)
    }
    collection.find(query).cursor[Notification].collect[Seq]().flatMap { notifs =>
      val projectIds = notifs.flatMap {
        case notif: ParticipationNotification => None
        case notif: InviteHackerNotification => Some(notif.projectId)
        case notif: AskProjectNotification => Some(notif.projectId)
      }
      Projects.findAllByIdWithHackers(projectIds).map { case (projects, hackers) =>
        (notifs, projects.map(p => (p.oid -> p)).toMap, hackers)
      }
    }
  }

  def delete(id: BSONObjectID): Future[Option[String]] = {
    collection.remove(BSONDocument("_id" -> id)).map(_.err)
  }

  def accept(notif: Notification): Future[Unit] = {
    notif match {
      case n:ParticipationNotification => for {
        event <- Events.findById(n.eventId)
        hacker <- Hackers.findById(n.hackerId)
        add <- Future.sequence((for {
          e <- event
          h <- hacker
          a = Events.addHacker(e, h)
        } yield a).toSeq)
      } yield ()
      case n:InviteHackerNotification => for {
        project <- Projects.findById(n.projectId)
        hacker <- Hackers.findById(n.hackerId)
        add <- Future.sequence((for {
          p <- project
          h <- hacker
          a = Projects.addTeammate(p, h)
        } yield a).toSeq)
      } yield ()
      case n:AskProjectNotification => for {
        project <- Projects.findById(n.projectId)
        hacker <- Hackers.findById(n.hackerId)
        add <- Future.sequence((for {
          p <- project
          h <- hacker
          a = Projects.addTeammate(p, h)
        } yield a).toSeq)
      } yield ()
    }
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
