package io.morgaroth.jenkinsclient

trait JenkinsError

case class RequestingError(description: String, requestId: String, cause: Throwable) extends JenkinsError

case class JenkinsHttpError(statusCode: Int, description: String, requestId: String, requestType: String, errorBody: Option[String]) extends JenkinsError

case class MarshallingError(description: String, requestId: String, cause: Throwable) extends JenkinsError

case class UnmarshallingError(description: String, requestId: String, cause: Throwable) extends JenkinsError

case class InvalidQueryError(description: String, cause: Throwable) extends JenkinsError

case class CustomJenkinsErrorImpl(info: String, requestId: String) extends JenkinsError

object CustomJenkinsError {
  def apply(info: String)(implicit rId: RequestId): JenkinsError = CustomJenkinsErrorImpl(info, rId.id)
}
