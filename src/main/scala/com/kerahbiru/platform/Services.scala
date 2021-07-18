package com.kerahbiru.platform

import cats.data.{EitherT, Reader}
import com.kerahbiru.platform.Config.{ThingManagement, UserClientRepo}
import com.kerahbiru.platform.Entities.{
  ClientId,
  ClientName,
  CreateClientDto,
  CreateClientResponse,
  UserClientItem,
  UserId
}
import facade.amazonaws.services.iot.{CertificateArn, PolicyName}
import io.circe.Json
import monix.eval.Task
import io.circe.parser.decode

import scala.scalajs.js
import scala.scalajs.js.JSON
import io.circe.syntax._
import net.exoego.facade.aws_lambda.{APIGatewayProxyEvent, APIGatewayProxyResult, AuthResponseContext}

case class Services(thingManagement: ThingManagement, clientRepo: UserClientRepo) {
  val tm   = thingManagement.tm
  val repo = clientRepo.repo

  def f(body: String): Task[Either[ServiceError, CreateClientDto]] =
    Task(
      decode[CreateClientDto](body) fold (_ => Left(RequestError), p => Right(p))
    )
  def createClient(userId: UserId, body: String): EitherT[Task, ServiceError, Json] =
    for {
      a <- EitherT(
        Task(
          decode[CreateClientDto](body) fold (_ => Left(RequestError), p => Right(p))
        )
      )
      b <- EitherT(
        Task(ClientName.create(a.name) fold (e => Left(e), p => Right(p)))
      )
      c <- EitherT.liftF[Task, ServiceError, ClientId](Task(ClientId.generate))
      d <- EitherT(tm.createPolicy(userId, c))
      e <- EitherT(tm.createCertificate())
      _ <- EitherT(tm.attachPolicyToCertificate(d, e.certificateArn))
      _ <- EitherT(tm.attachCertificateToThing(e.certificateArn))
      f <- EitherT(Task(Right(System.currentTimeMillis() / 1000).withLeft[ServiceError]))
      _ <- EitherT(repo.putClient(userId, UserClientItem(c, b, f, e.certificateArn, d)))
      g = CreateClientResponse(
        c,
        b,
        e.certificateArn,
        e.certificatePem,
        e.keyPair.PrivateKey.toOption.get,
        e.keyPair.PublicKey.toOption.get
      )
    } yield g.asJson

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
            value.claims.toOption match {
              case Some(value) => Right(UserId(value.sub))
              case None        => Left(ApiError("Cannot find claims.sub"))
            }
          case None => Left(ApiError("Cannot find context containing user"))
        }
      })
      b <- EitherT(Task {
        event.path match {
          case "/device/list"   => Right(listClients(a))
          case "/device/create" => Right(createClient(a, event.body.asInstanceOf[String]))
          case _                => Left(ApiError(s"${event.path} not recognized"))
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
