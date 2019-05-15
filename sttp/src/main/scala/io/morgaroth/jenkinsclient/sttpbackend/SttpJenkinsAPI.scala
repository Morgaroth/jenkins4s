package io.morgaroth.jenkinsclient.sttpbackend

import cats.Monad
import cats.data.EitherT
import cats.instances.future.catsStdInstancesForFuture
import cats.syntax.either._
import com.softwaremill.sttp._
import com.typesafe.scalalogging.{LazyLogging, Logger}
import io.morgaroth.jenkinsclient._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SttpJenkinsAPI(val config: JenkinsConfig, apiConfig: JenkinsAPIConfig)(implicit ex: ExecutionContext) extends JenkinsRestAPI[Future] with LazyLogging {

  override implicit val m: Monad[Future] = implicitly[Monad[Future]]

  implicit val backend: SttpBackend[Try, Nothing] = TryHttpURLConnectionBackend()
  private val requestsLogger = Logger(LoggerFactory.getLogger(getClass.getPackage.getName + ".requests"))

  override def invokeRequest(requestData: JenkinsRequest)(implicit requestId: RequestId): EitherT[Future, JenkinsError, JenkinsResponse] = {
    val u = requestData.render
    val requestWithoutPayload = sttp.method(requestData.method, uri"$u").headers(
      "Authorization" -> requestData.authToken,
      "Accept" -> "application/json",
      "User-Agent" -> "curl/7.61.0",
    )

    val request = requestData.payload.map { rawPayload =>
      requestWithoutPayload.body(rawPayload).contentType("application/json")
    }.getOrElse(requestWithoutPayload)

    if (apiConfig.debug) logger.debug(s"request to send: $request")
    requestsLogger.info(s"Request ID {}, request: {}, payload:\n{}", requestId.id, request.body("stripped"), request.body)

    val response = request
      .send()
      .toEither.leftMap[JenkinsError](RequestingError("try-http-backend-left", requestId.id, _))
      .flatMap { response =>
        if (apiConfig.debug) logger.debug(s"received response: $response")
        requestsLogger.info(s"Response ID {}, response: {}, payload:\n{}", requestId.id, response.copy(rawErrorBody = Right("stripped")), response.body.fold(identity, identity))
        response
          .rawErrorBody
          .leftMap(error => HttpError(response.code.intValue(), "http-response-error", requestId.id, Some(new String(error, "UTF-8"))))
          .map(body => JenkinsResponse(response.headers.toMap, body))
      }

    EitherT.fromEither[Future](response)
  }
}