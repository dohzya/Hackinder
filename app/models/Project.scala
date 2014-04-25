package models

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.Macros.Annotations.Key

case class Project(
  @Key("_id") oid: BSONObjectID,
  name: String,
  description: String,
  quote: String,
  leaderId: BSONObjectID,
  team: Seq[BSONObjectID]
) {
  def id = oid.stringify
}
object Project {
  def create(name: String, description: String, quote: String, leader: Hacker) = {
    Project(
      oid = BSONObjectID.generate,
      name = name,
      description = description,
      quote = quote,
      leaderId = leader.oid,
      team = Seq(leader.oid)
    )
  }
}
