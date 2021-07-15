package com.kerahbiru.platform

import cats.data.{EitherT, Reader}
import com.kerahbiru.platform.Config.{ThingManagement, UserClientRepo}
import com.kerahbiru.platform.Entities.{ClientId, UserClientItem, UserId}
import facade.amazonaws.services.iot.{CertificateArn, PolicyName}
import monix.eval.Task

import scala.scalajs.js
import scala.scalajs.js.JSON
import io.circe.syntax._

case class Services(thingManagement: ThingManagement, clientRepo: UserClientRepo) {
  val tm   = thingManagement.tm
  val repo = clientRepo.repo

  def createClient(userId: UserId): Task[js.Object] =
    (for {
      a <- EitherT.liftF[Task, ServiceError, ClientId](Task(ClientId.generate))
      b <- EitherT(tm.createPolicy(userId, a))
      c <- EitherT(tm.createCertificate())
      _ <- EitherT(tm.attachPolicyToCertificate(b, c.certificateArn))
      _ <- EitherT(tm.attachCertificateToThing(c.certificateArn))
      _ <- EitherT(repo.putClient(userId, UserClientItem(a, c.certificateArn, b)))
    } yield b).value
      .map(p => p.fold(e => js.Object(e.msg), q => js.Object(q)))

  def deleteClient(userId: UserId, clientId: ClientId): Task[js.Object] =
    (for {
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
    } yield ()).value
      .map(p => p.fold(e => js.Object(e.msg), _ => js.Object("Completed")))

  def listClients(userId: UserId): Task[js.Object] =
    (for {
      a <- EitherT(repo.listDevices(userId))
      b <- EitherT.liftF[Task, ServiceError, String](Task(a.asJson.toString()))

    } yield b).value
      .map(p => p.fold(e => js.Object(e.msg), q => js.Object(q)))

  def process(): Task[js.Object] =
    for {
      a <- listClients(UserId("martin"))
    } yield new js.Object(a)

}

object Services {

  val fromConfig: Reader[Config, Services] = for {
    a <- ThingManagement.fromConfig
    b <- UserClientRepo.fromConfig
  } yield Services(a, b)

  def start(services: Services): Task[js.Object] = services.process()
}
