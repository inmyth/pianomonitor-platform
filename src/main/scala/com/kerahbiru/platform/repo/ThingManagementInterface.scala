package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{CertCreationResponse, DeviceId}
import com.kerahbiru.platform.{Config, IotError, ServiceError}
import facade.amazonaws.services.cognitoidentity.IdentityId
import facade.amazonaws.services.iot.{CertificateArn, CertificateId, CertificatePem, Iot, PolicyName}
import monix.eval.Task

abstract class ThingManagementInterface(config: Config) {

  def createPolicy(identityId: IdentityId, deviceId: DeviceId): Task[Either[ServiceError, PolicyName]]

  def createCertificate(): Task[Either[ServiceError, CertCreationResponse]]

  def attachPolicyToCertificate(
      policyName: PolicyName,
      certificateArn: CertificateArn
  ): Task[Either[ServiceError, Unit]]

  def attachPolicyToUser(
      policyName: PolicyName,
      identityId: IdentityId
  ): Task[Either[ServiceError, Unit]]

  def attachCertificateToThing(certificateArn: CertificateArn): Task[Either[ServiceError, Unit]]

  def detachCertificateFromThing(certificateArn: CertificateArn): Task[Either[ServiceError, Unit]]

  def detachPolicyFromCertificate(
      policyName: PolicyName,
      certificateArn: CertificateArn
  ): Task[Either[ServiceError, Unit]]

  def detachPolicyFromUser(policyName: PolicyName, identityId: IdentityId): Task[Either[ServiceError, Unit]]

  def deleteCertificate(certificateId: CertificateId): Task[Either[ServiceError, Unit]]

  def deactivateCertificate(certificateId: CertificateId): Task[Either[ServiceError, Unit]]

  def deletePolicy(policyName: PolicyName): Task[Either[ServiceError, Unit]]

  def getCertificatePem(certificateId: CertificateId): Task[Either[ServiceError, CertificatePem]]
}

object ThingManagementInterface {

  def real(config: Config, iot: Iot) = new ThingManagementAwsIot(config, iot)

}
