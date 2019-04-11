package io.morgaroth.jenkinsclient

import java.net.URLEncoder.encode

import cats.Monad
import cats.data.EitherT
import io.circe.generic.auto._
import io.morgaroth.jenkinsclient.Methods.{Get, Post}
import io.morgaroth.jenkinsclient.marshalling.Jenkins4sMarshalling

import scala.language.{higherKinds, postfixOps}

trait JenkinsRestAPI[F[_]] extends Jenkins4sMarshalling {

  implicit def m: Monad[F]

  def config: JenkinsConfig

  private lazy val regGen = JenkinsRequest.forServer(config)

  protected def invokeRequest(request: JenkinsRequest)(implicit requestId: RequestId): EitherT[F, JenkinsError, String]

  def checkIfJobExists(jobId: String): EitherT[F, JenkinsError, Boolean] = {
    implicit val rId: RequestId = RequestId.newOne
    val req = regGen(Get, jobIdToPath(jobId), Nil, None)
    invokeRequest(req).map(_ => true).recover {
      case err =>
        println(err)
        false
    }
  }

  def buildJob(jobId: String, parameters: Iterable[BuildParam]): EitherT[F, JenkinsError, String] = {
    implicit val rId: RequestId = RequestId.newOne
    val payload = JenkinsBuildPayload(parameters.toVector)
    val req = regGen(Post, jobIdToPath(jobId) + "/build?delay=0sec", Nil, Some(MJson.write(payload)))
    invokeRequest(req)
  }


  def buildParametrizedJob(jobId: String, parameters: Iterable[BuildParam]): EitherT[F, JenkinsError, String] = {
    implicit val rId: RequestId = RequestId.newOne

    val query = parameters.foldLeft(StringBuilder.newBuilder.append(jobIdToPath(jobId)).append("/buildWithParameters?")) {
      case (b, p) => b ++= encode(p.name, "utf-8") ++= "=" ++= encode(p.value, "utf-8") ++= "&"
    }.mkString.dropRight(1)
    val req = regGen(Post, query, Nil, None)
    invokeRequest(req)
  }

  private def jobIdToPath(jobId: String) = {
    jobId.split("/").mkString("job/", "/job/", "")
  }
}
