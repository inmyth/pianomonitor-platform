package com.kerahbiru.platform

import cats.data.{EitherT, Reader}
import com.kerahbiru.platform.Config.{IdentityManagement, ThingManagement, UserDeviceRepo}
import com.kerahbiru.platform.Entities.{
  CreateDeviceDto,
  CreateDeviceResponse,
  DeleteDeviceDto,
  DeviceId,
  DeviceName,
  UserDeviceItem,
  UserId
}
import com.kerahbiru.platform.repo.{IdentityManagementCognito, ThingManagementInterface, UserDeviceRepoInterface}
import facade.amazonaws.services.cognitoidentity.IdentityId
import io.circe.Json
import io.circe.parser.decode
import io.circe.syntax._
import monix.eval.Task
import net.exoego.facade.aws_lambda.{APIGatewayProxyEvent, APIGatewayProxyResult, AuthResponseContext}

import scala.scalajs.js

case class Services(
    thingManagement: ThingManagement,
    userDeviceRepo: UserDeviceRepo,
    identityManagement: IdentityManagement
) {
  val tm: ThingManagementInterface  = thingManagement.tm
  val repo: UserDeviceRepoInterface = userDeviceRepo.repo
  val im: IdentityManagementCognito = identityManagement.im

  def createDevice(userId: UserId, identityId: IdentityId, body: String): EitherT[Task, ServiceError, Json] =
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
      d <- EitherT(tm.createPolicy(userId, c))
      e <- EitherT(tm.createCertificate())
      _ <- EitherT(tm.attachPolicyToCertificate(d, e.certificateArn))
      _ <- EitherT(tm.attachCertificateToThing(e.certificateArn))
      _ <- EitherT(tm.attachPolicyToUser(d, identityId))
      f <- EitherT.right(Task(System.currentTimeMillis() / 1000))
      _ <- EitherT(repo.putDevice(userId, UserDeviceItem(c, b, f, e.certificateArn, d)))
      g = CreateDeviceResponse(
        c,
        b,
        e.certificateArn,
        e.certificatePem,
        e.keyPair.PrivateKey.toOption.get,
        e.keyPair.PublicKey.toOption.get
      )
    } yield g.asJson

  def deleteDevice(userId: UserId, identityId: IdentityId, body: String): EitherT[Task, ServiceError, Json] =
    for {
      a <- EitherT(
        Task(
          decode[DeleteDeviceDto](body) fold (_ => Left(RequestError), p => Right(DeviceId(p.deviceId)))
        )
      )
      b <- EitherT(repo.getDevice(userId, a))
      certificateArn = b.certificateArn
      policyName     = b.policyName
      _ <- EitherT(tm.detachCertificateFromThing(certificateArn))
      _ <- EitherT(tm.detachPolicyFromCertificate(policyName, certificateArn))
      _ <- EitherT(tm.detachPolicyFromUser(policyName, identityId))
      certId = certificateArn.split("[/]").last
      _ <- EitherT(tm.deactivateCertificate(certId))
      _ <- EitherT(tm.deleteCertificate(certId))
      _ <- EitherT(tm.deletePolicy(policyName))
      _ <- EitherT(repo.deleteDevice(userId, a))
    } yield "{}".asJson

  def listDevices(userId: UserId): EitherT[Task, ServiceError, Json] =
    for {
      a <- EitherT(repo.listDevices(userId))
      b <- EitherT.liftF[Task, ServiceError, Json](Task(a.asJson))

    } yield b

  def process(event: APIGatewayProxyEvent): Task[APIGatewayProxyResult] =
    (for {
      a <- EitherT(Task {
        event.requestContext.authorizer.asInstanceOf[js.UndefOr[AuthResponseContext]].toOption match {
          case Some(value) =>
            value.claims.toOption match {
              case Some(value) => Right(UserId(value.sub))
              case None        => Left(ApiError("Cannot find claims.sub"))
            }
          case None => Left(ApiError("Cannot find context containing user"))
        }
      })
      b <- EitherT(im.getIdentityId(event.headers.get("Authorization").get))
      c <- EitherT(Task {
        event.path match {
          case "/device/list"   => Right(listDevices(a))
          case "/device/create" => Right(createDevice(a, event.body.asInstanceOf[String], b))
          case "/device/delete" => Right(deleteDevice(a, event.body.asInstanceOf[String], b))
          case _                => Left(ApiError(s"${event.path} not recognized"))
        }
      })
      d <- c
    } yield d).value.map(response)

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

}

object Services {

  val fromConfig: Reader[Config, Services] = for {
    a <- ThingManagement.fromConfig
    b <- UserDeviceRepo.fromConfig
    c <- IdentityManagement.fromConfig
  } yield Services(a, b, c)

}
