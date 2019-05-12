package io.morgaroth.jenkinsclient.marshalling

import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.{Decoder, DecodingFailure}
import io.morgaroth.jenkinsclient.models.{EnvInjectJobProperty, JenkinsJobProperty, ParametersDefinitionProperty}

trait JenkinsPropertyDecoder extends JenkinsParameterDefDecoder {

  implicit val jenkinsPropertyDecoder: Decoder[JenkinsJobProperty] = Decoder.instance { cursor =>
    val classField = cursor.downField("_class")
    classField.focus.map[Decoder.Result[JenkinsJobProperty]] {
      case paramsProperty if paramsProperty.asString.contains("hudson.model.ParametersDefinitionProperty") =>
        cursor.as[ParametersDefinitionProperty]
      case envInjectJobProperty if envInjectJobProperty.asString.contains("org.jenkinsci.plugins.envinject.EnvInjectJobProperty") =>
        cursor.as[EnvInjectJobProperty]
      case unknown =>
        DecodingFailure(s"unknown content of _class $unknown for build parameter", classField.history).asLeft
    }.getOrElse(DecodingFailure("missing _class field in parameter", cursor.history).asLeft)
  }
}

