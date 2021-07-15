package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{CertCreationResponse, ClientId, UserId}
import com.kerahbiru.platform.{Config, ServiceError}
import facade.amazonaws.services.iot.{CertificateArn, CertificateId, Iot, PolicyName}
import monix.eval.Task

abstract class ThingManagementInterface(config: Config) {

  def createPolicy(userId: UserId, clientId: ClientId): Task[Either[ServiceError, PolicyName]]

  def createCertificate(): Task[Either[ServiceError, CertCreationResponse]]

  def attachPolicyToCertificate(
      policyName: PolicyName,
      certificateArn: CertificateArn
  ): Task[Either[ServiceError, Unit]]

  def attachCertificateToThing(certificateArn: CertificateArn): Task[Either[ServiceError, Unit]]

  def detachCertificateFromThing(certificateArn: CertificateArn): Task[Either[ServiceError, Unit]]

  def detachPolicyFromCertificate(
      policyName: PolicyName,
      certificateArn: CertificateArn
  ): Task[Either[ServiceError, Unit]]

  def deleteCertificate(certificateId: CertificateId): Task[Either[ServiceError, Unit]]

  def deactivateCertificate(certificateId: CertificateId): Task[Either[ServiceError, Unit]]

  def deletePolicy(policyName: PolicyName): Task[Either[ServiceError, Unit]]

}

object ThingManagementInterface {

  def real(config: Config, iot: Iot) = new ThingManagementAwsIot(config, iot)

}
