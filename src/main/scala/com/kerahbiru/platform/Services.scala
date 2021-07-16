package com.kerahbiru.platform

import cats.data.{EitherT, Reader}
import com.kerahbiru.platform.Config.{ThingManagement, UserClientRepo}
import com.kerahbiru.platform.Entities.{ClientId, UserClientItem, UserId}
import facade.amazonaws.services.iot.{CertificateArn, PolicyName}
import io.circe.Json
import monix.eval.Task

import scala.scalajs.js
import scala.scalajs.js.JSON
import io.circe.syntax._
import net.exoego.facade.aws_lambda.{APIGatewayProxyEvent, APIGatewayProxyResult, AuthResponseContext}

case class Services(thingManagement: ThingManagement, clientRepo: UserClientRepo) {
  val tm   = thingManagement.tm
  val repo = clientRepo.repo

  def createClient(userId: UserId): EitherT[Task, ServiceError, Json] =
    for {
      a <- EitherT.liftF[Task, ServiceError, ClientId](Task(ClientId.generate))
      b <- EitherT(tm.createPolicy(userId, a))
      c <- EitherT(tm.createCertificate())
      _ <- EitherT(tm.attachPolicyToCertificate(b, c.certificateArn))
      _ <- EitherT(tm.attachCertificateToThing(c.certificateArn))
      _ <- EitherT(repo.putClient(userId, UserClientItem(a, c.certificateArn, b)))
    } yield b.asJson

  def deleteClient(userId: UserId, clientId: ClientId): EitherT[Task, ServiceError, Json] =
    for {
      a <- EitherT(repo.getClient(userId, clientId))
      certificateArn = a.certificateArn
      policyName     = a.policyName
      _ <- EitherT(tm.detachCertificateFromThing(certificateArn))
      _ <- EitherT(tm.detachPolicyFromCertificate(policyName, certificateArn))
      certId = certificateArn.split("[/]").last
      _ <- EitherT(tm.deactivateCertificate(certId))
      _ <- EitherT(tm.deleteCertificate(certId))
      _ <- EitherT(tm.deletePolicy(policyName))
      _ <- EitherT(repo.deleteClient(userId, clientId))
    } yield (Json.Null)

  def listClients(userId: UserId): EitherT[Task, ServiceError, Json] =
    for {
      a <- EitherT(repo.listDevices(userId))
      b <- EitherT.liftF[Task, ServiceError, Json](Task(a.asJson))

    } yield b

  def process(event: APIGatewayProxyEvent): Task[APIGatewayProxyResult] =
    (for {
      a <- EitherT(Task {
        event.requestContext.authorizer.asInstanceOf[js.UndefOr[AuthResponseContext]].toOption match {
          case Some(value) =>
            println(value)
            value.claims.toOption match {
              case Some(value) => Right(UserId(value.sub))
              case None        => Left(ApiError("Cannot find claims.sub"))
            }
          case None => Left(ApiError("Cannot find context containing user"))
        }

      })
      b <- EitherT(Task {
        event.path match {
          case "/device/list" => Right(listClients(a))
          case _              => Left(ApiError(s"${event.path} not recognized"))
        }
      })
      c <- b
    } yield c).value.map(response)

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
    b <- UserClientRepo.fromConfig
  } yield Services(a, b)

}
