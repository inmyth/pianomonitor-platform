package com.kerahbiru.platform

import cats.data.{EitherT, Reader}
import com.kerahbiru.platform.Config.{ThingManagement, UserClientRepo}
import com.kerahbiru.platform.Entities.{ClientId, UserId}
import facade.amazonaws.services.iot.{CertificateArn, PolicyName}
import monix.eval.Task

import scala.scalajs.js

case class Services(controlRepo: ThingManagement, clientRepo: UserClientRepo) {
  val control = controlRepo.control

  def createDevice(): Task[js.Object] =
    (for {
      a <- EitherT(control.createPolicy(UserId("martin"), ClientId.generate))
      b <- EitherT(control.createCertificate())
      _ <- EitherT(control.attachPolicyToCertificate(a, b.certificateArn))
      _ <- EitherT(control.attachCertificateToThing(b.certificateArn))
    } yield b).value
      .map(p => p.fold(e => js.Object(e.msg), q => js.Object(q)))

  def deleteDevice(certificateArn: CertificateArn, policyName: PolicyName): Task[js.Object] =
    (for {
      _ <- EitherT(control.detachCertificateFromThing(certificateArn))
      _ <- EitherT(control.detachPolicyFromCertificate(policyName, certificateArn))
      certId = certificateArn.split("[/]").last
      _ <- EitherT(control.deactivateCertificate(certId))
      _ <- EitherT(control.deleteCertificate(certId))
      _ <- EitherT(control.deletePolicy(policyName))
    } yield ()).value
      .map(p => p.fold(e => js.Object(e.msg), q => js.Object("Completed")))

  def process(): Task[js.Object] =
    for {
      a <- deleteDevice(
        "arn:aws:iot:us-west-2:734435107319:cert/1a95457cdce60875b613b2a9c80e6c97fd978a6cf3b7c761f319405ddf9610de",
        "martin_500099ed-16ec-477c-991d-f02a4f1ee424"
      )
    } yield new js.Object(a)

}

object Services {

  val fromConfig: Reader[Config, Services] = for {
    a <- ThingManagement.fromConfig
    b <- UserClientRepo.fromConfig
  } yield Services(a, b)

  def start(services: Services): Task[js.Object] = services.process()
}
