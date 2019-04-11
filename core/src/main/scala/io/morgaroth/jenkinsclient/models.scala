package io.morgaroth.jenkinsclient

import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Printer}

case class BuildParam(name: String, value: String)

object BuildParam {
  def json[A: Encoder](name: String, value: A, printer: Printer = Printer.noSpaces.copy(dropNullValues = true)): BuildParam =
    BuildParam(name, printer.pretty(value.asJson))
}

case class JenkinsBuildPayload(parameter: Vector[BuildParam])

case class BuildRef(number: Long, url: String)

trait BuildAction {
  def _class: String
}

case object EmptyAction extends BuildAction {
  val _class = "empty"
}

case class CauseAction(
                        _class: String,
                        causes: Vector[BuildCause]
                      ) extends BuildAction

trait BuildCause {
  def _class: String
}

case class UserIdCause(
                        _class: String,
                        shortDescription: String,
                        userId: String,
                        userName: String,
                      ) extends BuildCause

case class TriggerCause(
                         _class: String,
                         shortDescription: String,
                       ) extends BuildCause

case class UpsteamCause(
                         _class: String,
                         shortDescription: String,
                         upstreamBuild: Long,
                         upstreamProject: String,
                         upstreamUrl: String,
                       ) extends BuildCause

case class RebuildCause(
                         _class: String,
                         shortDescription: String,
                         upstreamBuild: Long,
                         upstreamProject: String,
                         upstreamUrl: String,
                       ) extends BuildCause

case class ParametersAction(
                             _class: String,
                             parameters: Vector[ParameterActionEntry]
                           ) extends BuildAction

trait ParameterActionEntry {
  def _class: String

  def name: String
}

case class StringParameterEntry(_class: String,
                                name: String,
                                value: String
                               ) extends ParameterActionEntry

case class BoolParameterEntry(
                               _class: String,
                               name: String,
                               value: Boolean
                             ) extends ParameterActionEntry


case class JenkinsBuildDetails(
                                buildNumber: Long,
                                buildResult: Option[String],
                              )

case class JenkinsBuildDetailsBox(
                                   _class: String,
                                   build: JenkinsBuildDetails,
                                 ) extends BuildAction

case class NotImportantAction(_class: String) extends BuildAction

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
                             description: String,
                             timestamp: Long,
                             previousBuild: Option[BuildRef],
                             nextBuild: Option[BuildRef],
                             actions: Vector[BuildAction],
                             artifacts: Vector[BuildArtifact],
                           )