package models

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.Macros.Annotations.Key

case class Project(
  @Key("_id") oid: BSONObjectID,
  name: String,
  leaderId: BSONObjectID,
  team: Seq[BSONObjectID]
) {
  def id = oid.stringify
}
object Project {
  def create(name: String, leader: Hacker) = {
    Project(
      oid = BSONObjectID.generate,
      name = name,
      leaderId = leader.oid,
      team = Seq(leader.oid)
    )
  }
}
