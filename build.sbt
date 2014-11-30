name := """aa-site-2.3"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.h2database" % "h2" % "1.3.148",
  "joda-time" % "joda-time" % "2.5",
  ws
)
