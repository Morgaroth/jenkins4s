package io.morgaroth.jenkinsclient.marshalling

import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.{Decoder, DecodingFailure}
import io.morgaroth.jenkinsclient.{BoolParameterEntry, ParameterActionEntry, StringParameterEntry}

trait BuildParameterDecoder {

  implicit val buildParameterDecoder: Decoder[ParameterActionEntry] = Decoder.instance { cursor =>
    val classField = cursor.downField("_class")
    classField.focus.map[Decoder.Result[ParameterActionEntry]] {
      case stringParam if stringParam.asString.contains("hudson.model.StringParameterValue") =>
        cursor.as[StringParameterEntry]
      case boolParam if boolParam.asString.contains("hudson.model.BooleanParameterValue") =>
        cursor.as[BoolParameterEntry]
      case unknown =>
        DecodingFailure(s"unknown content of _class $unknown for build parameter", classField.history).asLeft
    }.getOrElse(DecodingFailure("missing _class field in parameter", cursor.history).asLeft)
  }
}

object BuildParameterDecoder extends BuildParameterDecoder