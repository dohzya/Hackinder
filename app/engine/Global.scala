import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api._

import models.{ Hacker, Profile, Project }
import engine.{ Hackers, Projects }

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    val fhackers = Hackers.isEmpty.flatMap { empty =>
      if (empty) {
        Future.sequence {
          (1 to 10).map { i =>
            Profile(
              id = s"$i",
              email = s"hk$i@zengularity.com",
              verifiedEmail = true,
              name = s"Hacker $i",
              givenName = "Hacker",
              familyName = s"$i",
              link = "",
              gender = ""
            )
          }.map { profile =>
            Hackers.fromProfile(profile)
          }
        }
      }
      else Future.successful { Seq() }
    }
    fhackers.flatMap { hackers =>
      Projects.isEmpty.flatMap { empty =>
        if (!hackers.isEmpty && empty) {
          Future.sequence {
            (1 to 3).map { i =>
              Projects.insert(Project.create(
                name = s"Project $i",
                description = s"Description $i",
                quote = s"Quote $i",
                leader = hackers(i)
              )).flatMap { project =>
                Projects.addTeammate(project, hackers(hackers.length - i))
              }
            }
          }
        }
        else Future.successful { Seq() }
      }
    }
  }

}
