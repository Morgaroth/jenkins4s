package io.morgaroth.jenkinsclient.models

case class JenkinsJobInfo(
                           _class: String,
                           description: String,
                           displayName: String,
                           fullDisplayName: String,
                           fullName: String,
                           name: String,
                           url: String,
                           buildable: Boolean,
                           builds: Vector[JenkinsBuildRef],
                           inQueue: Boolean,
                           firstBuild: JenkinsBuildRef,
                           lastBuild: Option[JenkinsBuildRef],
                           lastCompletedBuild: Option[JenkinsBuildRef],
                           lastFailedBuild: Option[JenkinsBuildRef],
                           lastStableBuild: Option[JenkinsBuildRef],
                           lastSuccessfulBuild: Option[JenkinsBuildRef],
                           lastUnstableBuild: Option[JenkinsBuildRef],
                           lastUnsuccessfulBuild: Option[JenkinsBuildRef],
                           nextBuildNumber: Long,
                           property: Vector[JenkinsJobProperty],
                           //                           queueItem: AnyRef
                         )

case class JenkinsBuildRef(
                            number: Long,
                            url: String,
                          )

sealed trait JenkinsJobProperty

case class EnvInjectJobProperty() extends JenkinsJobProperty

case class ParametersDefinitionProperty(
                                         parameterDefinitions: Vector[JenkinsParamDef],
                                       ) extends JenkinsJobProperty

trait JenkinsParamDef {
  def name: String

  def description: String

  def `type`: String

  def defaultParameterValue: ParameterValue
}

case class BooleanParameterDef(
                                _class: String,
                                name: String,
                                `type`: String,
                                defaultParameterValue: BooleanParameterValue,
                                description: String,
                              ) extends JenkinsParamDef

case class StringParameterDef(
                               _class: String,
                               name: String,
                               `type`: String,
                               defaultParameterValue: StringParameterValue,
                               description: String,
                             ) extends JenkinsParamDef

case class ChoiceParameterDef(
                               _class: String,
                               name: String,
                               `type`: String,
                               defaultParameterValue: StringParameterValue,
                               choices: Set[String],
                               description: String,
                             ) extends JenkinsParamDef


sealed trait ParameterValue {
  def name: String
}

case class StringParameterValue(name: String, value: String) extends ParameterValue

case class BooleanParameterValue(name: String, value: Boolean) extends ParameterValue
