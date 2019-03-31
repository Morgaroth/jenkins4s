package io.morgaroth.jenkinsclient

import cats.Monad
import cats.data.EitherT
import io.morgaroth.jenkinsclient.Methods.Get
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

  def startJobWithParameters(jobId: String, parameters: Map[String, String]) = {
    val trueJobName = jobIdToPath(jobId)
  }

  private def jobIdToPath(jobId: String) = {
    jobId.split("/").mkString("job/", "/job/", "")
  }
}