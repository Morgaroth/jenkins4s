package io.morgaroth.jenkinsclient.marshalling

import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.{Decoder, DecodingFailure}
import io.morgaroth.jenkinsclient.models.{BooleanParameterDef, ChoiceParameterDef, JenkinsParamDef, StringParameterDef}

trait JenkinsParameterDefDecoder {

  implicit val jenkinsParameterDefDecoder: Decoder[JenkinsParamDef] = Decoder.instance { cursor =>
    val classField = cursor.downField("_class")
    classField.focus.map[Decoder.Result[JenkinsParamDef]] {
      case booleanParam if booleanParam.asString.contains("hudson.model.BooleanParameterDefinition") =>
        cursor.as[BooleanParameterDef]
      case stringParam if stringParam.asString.contains("hudson.model.StringParameterDefinition") =>
        cursor.as[StringParameterDef]
      case choicesParam if choicesParam.asString.contains("hudson.model.ChoiceParameterDefinition") =>
        cursor.as[ChoiceParameterDef]
      case unknown =>
        DecodingFailure(s"unknown content of _class $unknown for job parameter", classField.history).asLeft
    }.getOrElse(DecodingFailure("missing _class field in parameter", cursor.history).asLeft)
  }
}

