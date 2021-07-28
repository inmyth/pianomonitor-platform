package com.kerahbiru.platform

import cats.data.{EitherT, Reader}
import com.kerahbiru.platform.Config.{IdentityManagement, ThingManagement, UserDeviceRepo}
import com.kerahbiru.platform.Entities.{
  CreateDeviceDto,
  CreateDeviceResponse,
  DeleteDeviceDto,
  DeviceId,
  DeviceName,
  GetDeviceResponse,
  UserDeviceItem
}
import com.kerahbiru.platform.repo.{IdentityManagementCognito, ThingManagementInterface, UserDeviceRepoInterface}
import facade.amazonaws.services.cognitoidentity.IdentityId
import facade.amazonaws.services.iot.{CertificateArn, PolicyName}
import io.circe.Json
import io.circe.parser.decode
import io.circe.syntax._
import monix.eval.Task
import net.exoego.facade.aws_lambda.ALBEvent.QueryStringParameters
import net.exoego.facade.aws_lambda.{APIGatewayProxyEvent, APIGatewayProxyResult}

import scala.scalajs.js

case class Services(
    thingManagement: ThingManagement,
    userDeviceRepo: UserDeviceRepo,
    identityManagement: IdentityManagement
) {
  val tm: ThingManagementInterface  = thingManagement.tm
  val repo: UserDeviceRepoInterface = userDeviceRepo.repo
  val im: IdentityManagementCognito = identityManagement.im

  def createDevice(identityId: IdentityId, body: String): EitherT[Task, ServiceError, Json] =
    for {
      a <- EitherT(
        Task(
          decode[CreateDeviceDto](body) fold (_ => Left(RequestError), p => Right(p))
        )
      )
      b <- EitherT(
        Task(DeviceName.create(a.name) fold (e => Left(e), p => Right(p)))
      )
      c <- EitherT.liftF[Task, ServiceError, DeviceId](Task(DeviceId.generate))
      d <- EitherT(tm.createPolicy(identityId, c))
      e <- EitherT(tm.createCertificate())
      _ <- EitherT(tm.attachPolicyToCertificate(d, e.certificateArn))
      _ <- EitherT(tm.attachCertificateToThing(e.certificateArn))
      _ <- EitherT(tm.attachPolicyToUser(d, identityId))
      f <- EitherT.right(Task(System.currentTimeMillis() / 1000))
      _ <- EitherT(repo.putDevice(identityId, UserDeviceItem(c, b, f, e.certificateArn, d)))
      topic = getTopicFromPolicyName(d)
      g = CreateDeviceResponse(
        c,
        b,
        e.certificateArn,
        e.certificatePem,
        e.keyPair.PrivateKey.toOption.get,
        e.keyPair.PublicKey.toOption.get,
        topic
      )
    } yield g.asJson

  def deleteDevice(identityId: IdentityId, body: String): EitherT[Task, ServiceError, Json] =
    for {
      a <- EitherT(
        Task(
          decode[DeleteDeviceDto](body) fold (_ => Left(RequestError), p => Right(DeviceId(p.deviceId)))
        )
      )
      b <- EitherT(repo.getDevice(identityId, a))
      certificateArn = b.certificateArn
      policyName     = b.policyName
      _ <- EitherT(tm.detachCertificateFromThing(certificateArn))
      _ <- EitherT(tm.detachPolicyFromCertificate(policyName, certificateArn))
      _ <- EitherT(tm.detachPolicyFromUser(policyName, identityId))
      certId = certificateArn.split("[/]").last
      _ <- EitherT(tm.deactivateCertificate(certId))
      _ <- EitherT(tm.deleteCertificate(certId))
      _ <- EitherT(tm.deletePolicy(policyName))
      _ <- EitherT(repo.deleteDevice(identityId, a))
    } yield "{}".asJson

  def getDevice(identityId: IdentityId, params: Option[QueryStringParameters]): EitherT[Task, ServiceError, Json] =
    for {
      a <- EitherT(
        Task(
          (for {
            x <- params
            y <- x.get("deviceId")
            z = Right(DeviceId(y))
          } yield z).getOrElse(Left(ApiError("Bad param deviceId")))
        )
      )
      b <- EitherT(repo.getDevice(identityId, a))
      certId = b.certificateArn.split("[/]").last
      c <- EitherT(tm.getCertificatePem(certId))
      topic = getTopicFromPolicyName(b.policyName)
      d     = GetDeviceResponse(b.deviceId, b.deviceName, topic, c)
    } yield d.asJson

  def listDevices(identityId: IdentityId): EitherT[Task, ServiceError, Json] =
    for {
      a <- EitherT(repo.listDevices(identityId))
      b <- EitherT.liftF[Task, ServiceError, Json](Task(a.asJson))

    } yield b

  def process(event: APIGatewayProxyEvent): Task[APIGatewayProxyResult] = {
    def errorPath(path: String): EitherT[Task, ServiceError, Json] =
      EitherT.fromEither(Left(ApiError(s"$path not recognized")))
    (for {
      a <- EitherT(im.getIdentityId(event.headers.get("Authorization").get))
      b <- event.path match {
        case "/device/list"   => listDevices(a)
        case "/device/create" => createDevice(a, event.body.asInstanceOf[String])
        case "/device/delete" => deleteDevice(a, event.body.asInstanceOf[String])
        case "/device/get" =>
          getDevice(a, event.queryStringParameters.asInstanceOf[js.UndefOr[QueryStringParameters]].toOption)
        case _ => errorPath(event.path)
      }
    } yield b).value.map(response)
  }

  val response: Either[ServiceError, Json] => APIGatewayProxyResult = {
    case Left(e) =>
      if (e.level == SERVER) println(e.msg) // THE ONLY SIDE EFFECT
      val code = if (e.level == SERVER) 500 else 400
      APIGatewayProxyResult(
        headers = js
          .Dictionary("Access-Control-Allow-Origin" -> "*")
          .asInstanceOf[net.exoego.facade.aws_lambda.APIGatewayProxyResult.Headers],
        body = Json.Null.toString(),
        statusCode = code
      )

    case Right(v) =>
      APIGatewayProxyResult(
        headers = js
          .Dictionary("Access-Control-Allow-Origin" -> "*")
          .asInstanceOf[net.exoego.facade.aws_lambda.APIGatewayProxyResult.Headers],
        body = v.toString(),
        statusCode = 200
      )
  }

  def getTopicFromPolicyName(policyName: PolicyName): String = policyName.replace("_", "/")
}

object Services {

  val fromConfig: Reader[Config, Services] = for {
    a <- ThingManagement.fromConfig
    b <- UserDeviceRepo.fromConfig
    c <- IdentityManagement.fromConfig
  } yield Services(a, b, c)

}
