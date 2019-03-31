package io.morgaroth.jenkinsclient

trait JenkinsError

case class RequestingError(description: String, requestId: String, cause: Throwable) extends JenkinsError

case class HttpError(statusCode: Int, description: String, requestId: String, errorBody: Option[String]) extends JenkinsError

case class MarshallingError(description: String, requestId: String, cause: Throwable) extends JenkinsError

case class UnmarshallingError(description: String, requestId: String, cause: Throwable) extends JenkinsError

case class InvalidQueryError(description: String, cause: Throwable) extends JenkinsError
