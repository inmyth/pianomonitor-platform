package com.kerahbiru.platform

import facade.amazonaws.services.iot.{CertificateArn, CertificateId, CertificatePem, KeyPair, PolicyArn, PolicyName}
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveUnwrappedCodec, deriveUnwrappedDecoder, deriveUnwrappedEncoder}
import net.exoego.facade.aws_lambda.APIGatewayProxyEvent

import java.util.UUID
import scala.scalajs.js
//import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto._
import io.circe.generic.extras.defaults._
//import io.circe.generic.auto._
//import io.circe.parser.decode

object Entities {

  // topic : topic/userid/client111
  /*
  {
  [
    {
    "type": "temperature",
    "value": 30.1
    },
    {
    "type": "humidity",
    "value": 13
    }
  ]
  }


   */
  case class UserId(value: String) extends AnyVal

  case class ClientId(value: String) extends AnyVal

  case class ClientName(value: String) extends AnyVal

  object ClientName {

    def create(value: String): Either[ServiceError, ClientName] =
      if (value.length > 20) Left(ValidationError("Client name too long")) else Right(ClientName(value))

  }

  object ClientId {

    def generate: ClientId = ClientId(UUID.randomUUID().toString)
  }

  case class UserClientItem(
      clientId: ClientId,
      clientName: ClientName,
      creationTs: Long,
      certificateArn: CertificateArn,
      policyName: PolicyName
  )

  case class CertCreationResponse(certificateArn: CertificateArn, certificatePem: CertificatePem, keyPair: KeyPair)

  final case class HttpPayload(
      path: String,
      pathParameters: Map[String, String],
      body: String
  )

  case class CreateClientResponse(
      clientId: ClientId,
      clientName: ClientName,
      certificateArn: CertificateArn,
      certificatePem: CertificatePem,
      privateKey: String,
      publicKey: String
  )

//  object HttpPayload {
//
//    def build(
//        path: String,
//        pathParameters: Map[String, String],
//        body: String
//    ): HttpPayload =
//      HttpPayload(path, pathParameters, body)
//
//    def from(event: APIGatewayProxyEvent): HttpPayload =
//      HttpPayload.build(
//        event.path,
//        Option(event.pathParameters)
//          .map(_.asInstanceOf[js.Dictionary[String]].toMap[String, String])
//          .getOrElse(Map.empty[String, String]),
//        event.body.asInstanceOf[String]
//      )
//
//  }

  case class CreateClientDto(name: String)

  case class DeleteClientDto(clientId: String)

  implicit val DeleteClientDtoDecoder: Decoder[DeleteClientDto] = deriveDecoder

  implicit val CreateClientDtoDecoder: Decoder[CreateClientDto] = deriveDecoder

  implicit val ClientNameEncoder: Encoder[ClientName] = deriveUnwrappedEncoder

  implicit val ClientIdEncoder: Encoder[ClientId] = deriveUnwrappedEncoder

  implicit val UserClientItemEncoder: Encoder[UserClientItem] = deriveEncoder

  implicit val CreateClientResponseEncoder: Encoder[CreateClientResponse] = deriveEncoder

}
