name := "Hackinder"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "org.apache.commons" % "commons-email" % "1.2"
)

play.Project.playScalaSettings
