package models

import org.joda.time.DateTime

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.Macros.Annotations.Key

case class Event(
  @Key("_id") oid: BSONObjectID,
  name: String,
  projects: Seq[BSONObjectID],
  date: DateTime
// TODO later
/*
  pitchDate: DateTime
  teamDate: DateTime
*/
) {
  def id = oid.stringify
}
object Event {
  def create(name: String, date: DateTime) = {
    Event(
      oid = BSONObjectID.generate,
      name = name,
      projects = Nil,
      date = date
    )
  }
}
