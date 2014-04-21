package models

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.Macros.Annotations.Key

case class Profile(
  id: String,
  email: String,
  verifiedEmail: Boolean,
  name: String,
  givenName: String,
  familyName: String,
  link: String,
  picture: Option[String] = None,
  gender: String,
  birthday: Option[String] = None,
  locale: Option[String] = None
)
case class Hacker(
  @Key("_id") oid: BSONObjectID,
  trigram: String,
  profile: Profile
) {
  def id = oid.stringify
  def email = profile.email
  def name = profile.name
}
object Hacker {
  def create(profile: Profile) = {
    val rx = """^(...)@zen(exity|gularity).com$""".r
    val trigram = profile match {
      case rx(t) => t
      case _ => "???"
    }
    Hacker(
      oid = BSONObjectID.generate,
      trigram = trigram,
      profile = profile
    )
  }
}
