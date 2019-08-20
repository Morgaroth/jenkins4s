package io.morgaroth.jenkinsclient.marshalling

import io.circe.generic.auto._
import io.morgaroth.jenkinsclient.models.{JenkinsBuildInfo, JenkinsGlobalQueue, JenkinsJobInfo, JenkinsQueueItemInfo}
import org.scalatest.{EitherValues, Matchers}

import scala.io.Source

class Jenkins4sMarshallingTest extends org.scalatest.FlatSpec with Jenkins4sMarshalling with Matchers with EitherValues {

  behavior of "Jenkins4sMarshalling"

  it should "read build info" in {
    Vector("some_build_info_1.json", "some_build_info_2.json").foreach { resourceName =>
      val result = MJson.read[JenkinsBuildInfo](Source.fromResource(resourceName).mkString)
      result shouldBe 'right
      result.right.value
    }
  }

  it should "read job info" in {
    Vector("some_job_info_1.json").foreach { resourceName =>
      val result = MJson.read[JenkinsJobInfo](Source.fromResource(resourceName).mkString)
      result shouldBe 'right
      result.right.value
    }
  }

  it should "read a queue info" in {
    Vector("some_queue_info_1.json", "some_queue_info_2.json").foreach { resourceName =>
      val result = MJson.read[JenkinsQueueItemInfo](Source.fromResource(resourceName).mkString)
      result shouldBe 'right
      result.right.value
    }
  }

  it should "read a global queue info" in {
    Vector("global_queue_info_1.json").foreach { resourceName =>
      val result = MJson.read[JenkinsGlobalQueue](Source.fromResource(resourceName).mkString)
      result shouldBe 'right
      result.right.value
    }
  }
}
