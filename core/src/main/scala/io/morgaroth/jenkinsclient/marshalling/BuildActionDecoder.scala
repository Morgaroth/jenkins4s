package io.morgaroth.jenkinsclient.marshalling

import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.{Decoder, DecodingFailure}
import io.morgaroth.jenkinsclient.models._


trait BuildActionDecoder extends BuildParameterDecoder with BuildCauseDecoder {

  implicit val buildActionDecoder: Decoder[BuildAction] = Decoder.instance { cursor =>
    cursor.keys.map {
      case nonEmpty if nonEmpty.nonEmpty =>
        cursor.downField("_class").focus.map {
          case json if json.isString =>
            json.toString() match {
              case """"hudson.model.CauseAction"""" => cursor.as[CauseAction]
              case """"hudson.model.ParametersAction"""" => cursor.as[ParametersAction]
              case """"hudson.plugins.git.util.BuildDetails"""" => cursor.as[JenkinsBuildDetailsBox]
              case any => cursor.as[NotImportantAction]
            }
        }.getOrElse {
          DecodingFailure("missing __class", cursor.history).asLeft
        }
      case _ => EmptyAction.asRight
    }.getOrElse(DecodingFailure("dsa", cursor.history).asLeft)
  }
}
