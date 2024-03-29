name := """autoscout-car-adverts"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.web" %% "scalatestplus-web" % "4.0.1" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.25.1" % Test

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)
