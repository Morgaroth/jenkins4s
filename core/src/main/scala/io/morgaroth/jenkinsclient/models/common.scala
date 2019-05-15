package io.morgaroth.jenkinsclient.models


case class JenkinsQueuedBuildInfo(queueTicket: Long, url: String)


sealed trait BuildAction {
  def _class: String
}

case object EmptyAction extends BuildAction {
  val _class = "empty"
}

case class CauseAction(
                        _class: String,
                        causes: Vector[BuildCause]
                      ) extends BuildAction


case class ParametersAction(
                             _class: String,
                             parameters: Vector[ParameterActionEntry]
                           ) extends BuildAction

case class JenkinsBuildDetailsBox(
                                   _class: String,
                                   build: JenkinsBuildDetails,
                                 ) extends BuildAction

case class NotImportantAction(_class: String) extends BuildAction

sealed trait BuildCause {
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

sealed trait ParameterActionEntry {
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
