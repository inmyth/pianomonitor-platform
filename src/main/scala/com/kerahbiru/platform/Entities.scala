package com.kerahbiru.platform

import facade.amazonaws.services.iot.{CertificateArn, CertificatePem, KeyPair, PolicyName}
import io.circe.generic.extras.semiauto.deriveUnwrappedEncoder
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

import java.util.UUID

object Entities {

  case class DeviceId(value: String) extends AnyVal

  case class DeviceName(value: String) extends AnyVal

  object DeviceName {

    def create(value: String): Either[ServiceError, DeviceName] =
      if (value.length > 30) Left(ValidationError("Device name too long")) else Right(DeviceName(value))

  }

  object DeviceId {

    def generate: DeviceId = DeviceId(UUID.randomUUID().toString.split("[-]").head)
  }

  case class UserDeviceItem(
      deviceId: DeviceId,
      deviceName: DeviceName,
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

  case class CreateDeviceResponse(
      deviceId: DeviceId,
      deviceName: DeviceName,
      certificateArn: CertificateArn,
      certificatePem: CertificatePem,
      privateKey: String,
      publicKey: String
  )

  case class CreateDeviceDto(name: String)

  case class DeleteDeviceDto(deviceId: String)

  implicit val DeleteDeviceDtoDecoder: Decoder[DeleteDeviceDto] = deriveDecoder

  implicit val CreateDeviceDtoDecoder: Decoder[CreateDeviceDto] = deriveDecoder

  implicit val DeviceNameEncoder: Encoder[DeviceName] = deriveUnwrappedEncoder

  implicit val DeviceIdEncoder: Encoder[DeviceId] = deriveUnwrappedEncoder

  implicit val UserDeviceItemEncoder: Encoder[UserDeviceItem] = deriveEncoder

  implicit val CreateDeviceResponseEncoder: Encoder[CreateDeviceResponse] = deriveEncoder

}
