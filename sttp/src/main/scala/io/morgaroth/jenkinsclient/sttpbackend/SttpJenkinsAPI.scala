package io.morgaroth.jenkinsclient.sttpbackend

import cats.Monad
import cats.data.EitherT
import cats.instances.future.catsStdInstancesForFuture
import cats.syntax.either._
import com.typesafe.scalalogging.{LazyLogging, Logger}
import io.morgaroth.jenkinsclient._
import org.slf4j.LoggerFactory
import sttp.client._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SttpJenkinsAPI(val config: JenkinsConfig, apiConfig: JenkinsAPIConfig)(implicit ex: ExecutionContext) extends JenkinsRestAPI[Future] with LazyLogging {

  override implicit val m: Monad[Future] = implicitly[Monad[Future]]

  implicit val backend: SttpBackend[Try, Nothing, NothingT] = TryHttpURLConnectionBackend()
  private val requestsLogger = Logger(LoggerFactory.getLogger(getClass.getPackage.getName + ".requests"))

  override def invokeRequest(requestData: JenkinsRequest)(implicit requestId: RequestId): EitherT[Future, JenkinsError, JenkinsResponse] = {
    val u = requestData.render
    val requestWithoutPayload = basicRequest.method(requestData.method, uri"$u")
      .header("Authorization", requestData.authToken)
      .header("Accept", "application/json")
      .header("User-Agent", "curl/7.61.0")
      .followRedirects(false)

    val request = requestData.payload match {
      case NoPayload => requestWithoutPayload
      case JsonString(serializedJson) => requestWithoutPayload.body(serializedJson).contentType("application/json")
      case Form(data) => requestWithoutPayload.body(data)
    }

    if (apiConfig.debug) logger.debug(s"request to send: $request")
    requestsLogger.info(s"Request ID {}, request: {}, payload:\n{}", requestId.id, request.body("stripped"), request.body)

    val response = request
      .send()
      .toEither.leftMap[JenkinsError](RequestingError("try-http-backend-left", requestId.id, _))
      .flatMap { response =>
        if (apiConfig.debug) logger.debug(s"received response: $response")
        requestsLogger.info(s"Request ID {}, response: {}, payload:\n{}", requestId, response.copy(body = response.body.bimap(_ => "There is an error body", _ => "There is a success body")), response.body.fold(identity, identity), response.body.fold(identity, identity))
        response.body
          .leftMap(error => JenkinsHttpError(response.code.code, "http-response-error", requestId.id, requestId.kind, Some(error)))
          .map(body => JenkinsResponse(response.code.code, response.headers.map(x => x.name -> x.value).toMap, body))
          .recover {
            case JenkinsHttpError(302, _, _, _, Some(data)) =>
              JenkinsResponse(302, response.headers.map(x => x.name -> x.value).toMap, data)
          }
      }

    EitherT.fromEither[Future](response)
  }
}