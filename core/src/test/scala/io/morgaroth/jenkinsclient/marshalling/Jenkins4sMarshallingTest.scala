package io.morgaroth.jenkinsclient.marshalling

import io.circe.generic.auto._
import io.morgaroth.jenkinsclient.JenkinsBuildInfo
import org.scalatest.{EitherValues, Matchers}

import scala.io.Source

class Jenkins4sMarshallingTest extends org.scalatest.FlatSpec with Jenkins4sMarshalling with Matchers with EitherValues {

  behavior of "Jenkins4sMarshalling"

  it should "read build info" in {
    val result = MJson.read[JenkinsBuildInfo](Source.fromResource("some_build_info_1.json").mkString)
    result shouldBe 'right
    result.right.value
  }
}
