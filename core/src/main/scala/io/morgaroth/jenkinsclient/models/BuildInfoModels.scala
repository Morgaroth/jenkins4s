package io.morgaroth.jenkinsclient.models

import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Printer}

case class BuildParam(name: String, value: String)

object BuildParam {
  def json[A: Encoder](name: String, value: A, printer: Printer = Printer.noSpaces.copy(dropNullValues = true)): BuildParam =
    BuildParam(name, printer.print(value.asJson))
}

case class JenkinsBuildPayload(parameter: Vector[BuildParam])

case class BuildRef(number: Long, url: String)

case class BuildArtifact(
                          displayPath: String,
                          fileName: String,
                          relativePath: String,
                        )

case class JenkinsBuildInfo(
                             _class: String,
                             fullDisplayName: String,
                             id: String,
                             url: String,
                             result: Option[String],
                             displayName: String,
                             number: Long,
                             duration: Long,
                             building: Boolean,
                             description: Option[String],
                             timestamp: Long,
                             previousBuild: Option[BuildRef],
                             nextBuild: Option[BuildRef],
                             actions: Vector[BuildAction],
                             artifacts: Vector[BuildArtifact],
                           )