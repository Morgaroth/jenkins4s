package io.morgaroth.jenkinsclient.models

sealed abstract case class BuildSymbolic(str: String)

object BuildSymbolic {

  object Last extends BuildSymbolic("lastBuild")

  object LastStable extends BuildSymbolic("lastStableBuild")

  object LastSuccessful extends BuildSymbolic("lastSuccessfulBuild")

  object LastFailed extends BuildSymbolic("lastFailedBuild")

  object LastUnsuccessful extends BuildSymbolic("lastUnsuccessfulBuild")

  object LastCompleted extends BuildSymbolic("lastCompletedBuild")

  object First extends BuildSymbolic("firstBuild")

}