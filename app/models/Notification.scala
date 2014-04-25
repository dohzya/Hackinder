package models

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.Macros.Annotations.Key

sealed trait Notification {
  def oid: BSONObjectID
  def typ: String
  def id = oid.stringify
}

case class ParticipationNotification(
  oid: BSONObjectID,
  eventId: BSONObjectID,
  hackerId: BSONObjectID
) extends Notification { def typ = ParticipationNotification.typ }
object ParticipationNotification {
  val typ = "participation"
  def create(event: Event, hackerId: BSONObjectID) = ParticipationNotification(
    oid = BSONObjectID.generate,
    eventId = event.oid,
    hackerId = hackerId
  )
}

case class InviteHackerNotification(
  oid: BSONObjectID,
  projectId: BSONObjectID,
  hackerId: BSONObjectID
) extends Notification { def typ = InviteHackerNotification.typ }
object InviteHackerNotification {
  val typ = "inviteHacker"
  def create(project: Project, hackerId: BSONObjectID) = InviteHackerNotification(
    oid = BSONObjectID.generate,
    projectId = project.oid,
    hackerId = hackerId
  )
}

case class AskProjectNotification(
  oid: BSONObjectID,
  hackerId: BSONObjectID,
  projectId: BSONObjectID
) extends Notification { def typ = AskProjectNotification.typ }
object AskProjectNotification {
  val typ = "askProject"
  def create(hacker: Hacker, projectId: BSONObjectID) = AskProjectNotification(
    oid = BSONObjectID.generate,
    hackerId = hacker.oid,
    projectId = projectId
  )
}
