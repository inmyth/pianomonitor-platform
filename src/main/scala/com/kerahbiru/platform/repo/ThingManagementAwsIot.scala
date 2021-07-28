package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{CertCreationResponse, DeviceId}
import com.kerahbiru.platform.repo.ThingManagementAwsIot.createPolicyJson
import com.kerahbiru.platform.{Config, Entities, IotError, ServiceError}
import facade.amazonaws.services.cognitoidentity.IdentityId
import facade.amazonaws.services.iot.{
  AttachPolicyRequest,
  AttachThingPrincipalRequest,
  CertificateArn,
  CertificateId,
  CertificatePem,
  CertificateStatus,
  CreateKeysAndCertificateRequest,
  CreatePolicyRequest,
  DeleteCertificateRequest,
  DeletePolicyRequest,
  DescribeCertificateRequest,
  DetachPolicyRequest,
  DetachThingPrincipalRequest,
  Iot,
  PolicyName,
  UpdateCertificateRequest
}
import monix.eval.Task

class ThingManagementAwsIot(config: Config, iot: Iot) extends ThingManagementInterface(config) {

  override def createPolicy(
      identityId: IdentityId,
      deviceId: Entities.DeviceId
  ): Task[Either[ServiceError, PolicyName]] =
    Task
      .fromFuture {
        iot.createPolicyFuture(
          CreatePolicyRequest(
            policyDocument = createPolicyJson(config.awsAccount, config.region, identityId, deviceId),
            policyName = s"""${identityId.replace(":", "-")}_${deviceId.value}"""
          )
        )
      }
      .map(p =>
        p.policyName.toOption match {
          case Some(value) => Right(value)
          case None        => Left(IotError("Policy creation response empty"))
        }
      )
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def createCertificate(): Task[Either[ServiceError, CertCreationResponse]] =
    Task
      .fromFuture(
        iot.createKeysAndCertificateFuture(
          CreateKeysAndCertificateRequest(
            setAsActive = true
          )
        )
      )
      .map(p =>
        (p.certificateArn.toOption, p.certificateId.toOption, p.certificatePem.toOption, p.keyPair.toOption) match {
          case (Some(certArn), Some(_), Some(certPem), Some(keyPair)) =>
            Right(CertCreationResponse(certArn, certPem, keyPair))
          case _ => Left(IotError("Certificate creation response empty"))
        }
      )
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def attachPolicyToCertificate(
      policyName: PolicyName,
      certificateArn: CertificateArn
  ): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        iot.attachPolicyFuture(
          AttachPolicyRequest(
            policyName = policyName,
            target = certificateArn
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def attachPolicyToUser(policyName: PolicyName, identityId: IdentityId): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        iot.attachPolicyFuture(
          AttachPolicyRequest(
            policyName = policyName,
            target = identityId
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def attachCertificateToThing(
      certificateArn: CertificateArn
  ): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        iot.attachThingPrincipalFuture(
          AttachThingPrincipalRequest(
            principal = certificateArn,
            thingName = config.thingName
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def detachCertificateFromThing(certificateArn: CertificateArn): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        iot.detachThingPrincipalFuture(
          DetachThingPrincipalRequest(
            thingName = config.thingName,
            principal = certificateArn
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def detachPolicyFromCertificate(
      policyName: PolicyName,
      certificateArn: CertificateArn
  ): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        iot.detachPolicyFuture(
          DetachPolicyRequest(
            policyName = policyName,
            target = certificateArn
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def detachPolicyFromUser(policyName: PolicyName, identityId: IdentityId): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        iot.detachPolicyFuture(
          DetachPolicyRequest(
            policyName = policyName,
            target = identityId
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def deleteCertificate(certificateId: CertificateId): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        iot.deleteCertificateFuture(
          DeleteCertificateRequest(
            certificateId = certificateId,
            forceDelete = true
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def deletePolicy(policyName: PolicyName): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        iot.deletePolicyFuture(
          DeletePolicyRequest(
            policyName = policyName
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def deactivateCertificate(certificateId: CertificateId): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        iot.updateCertificateFuture(
          UpdateCertificateRequest(
            certificateId = certificateId,
            newStatus = CertificateStatus.INACTIVE
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(IotError(e.getMessage)))

  override def getCertificatePem(certificateId: CertificateId): Task[Either[IotError, CertificatePem]] =
    Task
      .fromFuture(
        iot.describeCertificateFuture(
          DescribeCertificateRequest(
            certificateId = certificateId
          )
        )
      )
      .map(p =>
        p.certificateDescription.toOption match {
          case Some(value) => Right(value.certificatePem.toOption.get)
          case None        => Left(IotError("Certificate description empty"))
        }
      )
      .onErrorHandle(e => Left(IotError(e.getMessage)))
}

object ThingManagementAwsIot {

  def createPolicyJson(principal: String, region: String, identityId: IdentityId, deviceId: DeviceId): String =
    s"""{
      |"Version": "2012-10-17",
      |"Statement": [
      |  {
      |    "Effect": "Allow",
      |    "Action": "iot:Subscribe",
      |    "Resource": "arn:aws:iot:$region:$principal:topicfilter/$identityId/${deviceId.value}"
      |  },
      |  {
      |    "Effect": "Allow",
      |    "Action": "iot:Connect",
      |    "Resource": "arn:aws:iot:$region:$principal:client/*"
      |  },
      |  {
      |    "Effect": "Allow",
      |    "Action": [
      |        "iot:Publish",
      |        "iot:Receive",
      |        "iot:Republish"
      |      ],
      |    "Resource": "arn:aws:iot:$region:$principal:topic/$identityId/${deviceId.value}"
      |  }
      |]
      |}""".stripMargin

}
