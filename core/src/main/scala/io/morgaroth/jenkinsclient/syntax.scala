package io.morgaroth.jenkinsclient

import scala.language.implicitConversions

sealed trait JenkinsQuery {
  def render: String
}

class Param(name: String, value: String) extends JenkinsQuery {
  lazy val render: String = s"$name=$value"
}

trait Method

object Methods {

  object Get extends Method

  object Post extends Method

  object Put extends Method

  object Delete extends Method

}

trait Payload

case object NoPayload extends Payload

case class JsonString(serializedJson: String) extends Payload

case class Form(data: Map[String, String]) extends Payload

case class JenkinsRequest(
                           service: String,
                           authToken: String,
                           method: Method,
                           path: String,
                           query: List[JenkinsQuery],
                           payload: Payload,
                         ) {
  lazy val render: String = {
    val base = s"$service/$path"
    if (query.nonEmpty) {
      s"$base?${query.map(_.render).mkString("&")}"
    } else base
  }
}

object JenkinsRequest {
  def forServer(cfg: JenkinsConfig): (Method, String, List[JenkinsQuery], Payload) => JenkinsRequest = new JenkinsRequest(cfg.address, cfg.getBasicAuthHeaderValue, _, _, _, _)
}

case class JenkinsResponse(
                            code: Int,
                            headers: Map[String, String],
                            payload: String,
                          )