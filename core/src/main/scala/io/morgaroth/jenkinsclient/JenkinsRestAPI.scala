package io.morgaroth.jenkinsclient

import java.net.URLEncoder.encode

import cats.Monad
import cats.data.EitherT
import io.circe.generic.auto._
import io.morgaroth.jenkinsclient.Methods.{Get, Post}
import io.morgaroth.jenkinsclient.marshalling.Jenkins4sMarshalling
import io.morgaroth.jenkinsclient.models._

import scala.language.{higherKinds, postfixOps}

trait JenkinsRestAPI[F[_]] extends Jenkins4sMarshalling {

  implicit def m: Monad[F]

  def config: JenkinsConfig

  private lazy val regGen = JenkinsRequest.forServer(config)

  protected def invokeRequest(request: JenkinsRequest)(implicit requestId: RequestId): EitherT[F, JenkinsError, JenkinsResponse]

  def checkIfJobExists(jobId: String): EitherT[F, JenkinsError, Boolean] = {
    implicit val rId: RequestId = RequestId.newOne("check-job-exists")
    val req = regGen(Get, jobIdToPath(jobId), Nil, NoPayload)
    invokeRequest(req).map(_ => true).recover {
      case err =>
        println(err)
        false
    }
  }

  def buildJob(jobId: String, parameters: Iterable[BuildParam]): EitherT[F, JenkinsError, JenkinsQueuedBuildInfo] = {
    implicit val rId: RequestId = RequestId.newOne("build-job")
    val payload = JenkinsBuildPayload(parameters.toVector)
    val req = regGen(Post, jobIdToPath(jobId) + "/build", Nil, Form(Map("json" -> MJson.write(payload))))

    for {
      url <- invokeRequest(req).flatMap(resp => EitherT.fromOption(resp.headers.get("Location"), CustomJenkinsError(s"No Location header among ${resp.headers}")))
      queue <- globalQueueInfo()
      thisJobsQueueItems = queue.items.filter(_.task.url == url)
      notSoLot <- EitherT.cond(thisJobsQueueItems.size <= 1, thisJobsQueueItems.headOption, CustomJenkinsError(s"more than one ticket for job $url! needs further investigating"))
      exactOne <- EitherT.fromOption(notSoLot, CustomJenkinsError(s"missing queue item for $url"))
    } yield JenkinsQueuedBuildInfo(exactOne.id, exactOne.url)
  }

  def buildInfo(jobId: String, buildSymbolic: BuildSymbolic): EitherT[F, JenkinsError, JenkinsBuildInfo] = {
    buildInfo(jobId, buildSymbolic.str)
  }

  def buildInfo(jobId: String, buildNumber: Long): EitherT[F, JenkinsError, JenkinsBuildInfo] = {
    buildInfo(jobId, buildNumber.toString)
  }

  def buildInfo(jobId: String, build: String): EitherT[F, JenkinsError, JenkinsBuildInfo] = {
    implicit val rId: RequestId = RequestId.newOne("build-info")
    val req = regGen(Get, jobIdToPath(jobId) + s"/$build/api/json", Nil, NoPayload)
    invokeRequest(req).map(_.payload).flatMap(MJson.readT[F, JenkinsBuildInfo]).map { info =>
      info.copy(actions = info.actions.filter(_ != EmptyAction))
    }
  }

  def buildParametrizedJob(jobId: String, parameters: Iterable[BuildParam]): EitherT[F, JenkinsError, JenkinsQueuedBuildInfo] = {
    implicit val rId: RequestId = RequestId.newOne("build-parametrized-job")

    val query = parameters.foldLeft(new StringBuilder().append(jobIdToPath(jobId)).append("/buildWithParameters?")) {
      case (b, p) => b ++= encode(p.name, "utf-8") ++= "=" ++= encode(p.value, "utf-8") ++= "&"
    }.mkString.dropRight(1)
    val req = regGen(Post, query, Nil, NoPayload)
    invokeRequest(req).map { resp =>
      val url = resp.headers("Location")
      JenkinsQueuedBuildInfo(url.split("/").filter(_.nonEmpty).last.toLong, url)
    }
  }

  def jobInfo(jobId: String): EitherT[F, JenkinsError, JenkinsJobInfo] = {
    implicit val rId: RequestId = RequestId.newOne("job-info")
    val req = regGen(Get, jobIdToPath(jobId) + "/api/json", Nil, NoPayload)
    invokeRequest(req).map(_.payload).flatMap(MJson.readT[F, JenkinsJobInfo])
  }

  def queueTicketInfo(queueId: Long): EitherT[F, JenkinsError, JenkinsQueueItemInfo] = {
    implicit val rId: RequestId = RequestId.newOne("queue-ticket-info")
    val req = regGen(Get, s"queue/item/$queueId/api/json", Nil, NoPayload)
    invokeRequest(req).map(_.payload).flatMap(MJson.readT[F, JenkinsQueueItemInfo])
  }

  def globalQueueInfo(): EitherT[F, JenkinsError, JenkinsGlobalQueue] = {
    implicit val rId: RequestId = RequestId.newOne("global-queue-info")
    val req = regGen(Get, s"queue/api/json", Nil, NoPayload)
    invokeRequest(req).map(_.payload).flatMap(MJson.readT[F, JenkinsGlobalQueue])
  }

  private def jobIdToPath(jobId: String) = {
    jobId.split("/").mkString("job/", "/job/", "")
  }
}
