val akkaV = "2.5.6"
val akkaHttpVer = "10.0.9"

val circeVersion = "0.12.3"

val validate = Def.taskKey[Unit]("Validates entire project")

val crossScalaVersionsValues = Seq("2.12.10", "2.13.1")

val commonSettings = Seq(
  organization := "io.morgaroth",
  scalaVersion := "2.13.1",
  crossScalaVersions := crossScalaVersionsValues,

  resolvers ++= Seq(
    ("Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/").withAllowInsecureProtocol(true),
    Resolver.bintrayRepo("morgaroth", "maven").withAllowInsecureProtocol(true),
  ),
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),

  javaOptions in Test += "-Duser.timezone=UTC",
  logBuffered := false,

  // Bintray
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayVcsUrl := Some("https://gitlab.com/morgaroth/jenkins4s"),
)


val core = project
  .settings(commonSettings: _*)
  .settings(
    name := "jenkins4s-core",
    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.10.1",
      "org.typelevel" %% "cats-core" % "2.0.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.typesafe" % "config" % "1.3.3",

      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
    )
  )

val sttp = project.in(file("sttp")).dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "jenkins4s-sttp",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client" %% "core" % "2.0.0-RC5",
    )
  )

val akka = project.in(file("akka-http")).dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "jenkins4s-akka-http",
    libraryDependencies ++= Seq(

    )
  )

val jenkins4s = project.in(file(".")).aggregate(core, sttp, akka)
  .settings(
    name := "jenkins4s",
    publish := {},
    crossScalaVersions := crossScalaVersionsValues,

    validate := Def.task {
      (Test / test).value
      //      tut.value
    }.value,

    // Release
    releaseTagComment := s"Releasing ${(version in ThisBuild).value} [skip ci]",
    releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]",
    releaseNextCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]",
    releaseCrossBuild := true,
  )