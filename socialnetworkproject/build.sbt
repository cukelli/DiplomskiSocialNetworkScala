organization := "com.novalite"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.12"
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.26"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
libraryDependencies += "com.pauldijou" %% "jwt-play" % "5.0.0"
libraryDependencies += "com.pauldijou" %% "jwt-core" % "5.0.0"
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

libraryDependencies += "de.svenkubiak" % "jBCrypt" % "0.4.1"

//javaOptions += "-Xdebug"
//javaOptions += "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
