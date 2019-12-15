package io.morgaroth.jenkinsclient.marshalling

import cats.Monad
import cats.data.EitherT
import cats.syntax.either._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.morgaroth.jenkinsclient.{JenkinsError, RequestId, UnmarshallingError}

import scala.language.{higherKinds, implicitConversions}

trait Jenkins4sMarshalling extends JodaCodec with BuildActionDecoder with JenkinsPropertyDecoder {

  implicit class Extractable(value: JsonObject) {
    def extract[T](implicit decoder: Decoder[T]): Either[Error, T] = decode[T](value.toString)
  }

  object MJson {
    def read[T](str: String)(implicit d: Decoder[T]): Either[Error, T] = decode[T](str)

    def readT[F[_], T](str: String)(implicit d: Decoder[T], m: Monad[F], requestId: RequestId): EitherT[F, JenkinsError, T] =
      EitherT.fromEither(read[T](str).leftMap[JenkinsError](e => UnmarshallingError(e.getMessage, requestId.id, e)))

    def write[T](value: T)(implicit d: Encoder[T]): String = Printer.noSpaces.copy(dropNullValues = true).print(value.asJson)

    def writePretty[T](value: T)(implicit d: Encoder[T]): String = printer.print(value.asJson)
  }

  // keep all special settings with method write above
  implicit val printer: Printer = Printer.spaces2.copy(dropNullValues = true)
}

object Jenkins4sMarshalling extends Jenkins4sMarshalling