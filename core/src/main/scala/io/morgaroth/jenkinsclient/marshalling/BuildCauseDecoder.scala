package io.morgaroth.jenkinsclient.marshalling


import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.{Decoder, DecodingFailure}
import io.morgaroth.jenkinsclient._
import io.morgaroth.jenkinsclient.models.{BuildCause, RebuildCause, TriggerCause, UpsteamCause, UserIdCause}

trait BuildCauseDecoder {

  implicit val buildCauseDecoder: Decoder[BuildCause] = Decoder.instance { cursor =>
    val classField = cursor.downField("_class")
    classField.focus.map[Decoder.Result[BuildCause]] {
      case rebuildCause if rebuildCause.asString.contains("com.sonyericsson.rebuild.RebuildCause") => cursor.as[RebuildCause]
      case userCause if userCause.asString.contains("hudson.model.Cause$UserIdCause") => cursor.as[UserIdCause]
      case upstreamCause if upstreamCause.asString.contains("hudson.model.Cause$UpstreamCause") => cursor.as[UpsteamCause]
      case triggerCause if triggerCause.asString.contains("hudson.triggers.TimerTrigger$TimerTriggerCause") => cursor.as[TriggerCause]
      case unknown =>
        DecodingFailure(s"unknown content of _class $unknown for cause action", classField.history).asLeft
    }.getOrElse(DecodingFailure("missing _class field in buildCause", cursor.history).asLeft)
  }
}

object BuildCauseDecoder extends BuildParameterDecoder