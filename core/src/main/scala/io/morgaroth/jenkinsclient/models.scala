package io.morgaroth.jenkinsclient

import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Printer}

case class BuildParam(name: String, value: String)

object BuildParam {
  def json[A: Encoder](name: String, value: A, printer: Printer = Printer.noSpaces.copy(dropNullValues = true)): BuildParam =
    BuildParam(name, printer.pretty(value.asJson))
}

case class JenkinsBuildPayload(parameter: Vector[BuildParam])