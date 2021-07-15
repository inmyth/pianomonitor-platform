package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{ClientId, UserId}
import com.kerahbiru.platform.{Config, DomainError}
import facade.amazonaws.services.iot.{CreateKeysAndCertificateResponse, Iot, PolicyArn}
import monix.eval.Task

abstract class IControlRepo(config: Config) {

  def createPolicy(userId: UserId, clientId: ClientId): Task[Either[DomainError, PolicyArn]]

  def createCertificate(): Task[Either[DomainError, CreateKeysAndCertificateResponse]]

//  def storeAsDevice(userId: UserId, clientId: ClientId, certificateArn: CertificateArn, policyArn: PolicyArn) = ???

  def attachPolicyToCertificate() = ???

  def detachPolicyFromCertificate() = ???

  def attachCertificateToThing() = ???

  def detachCertificateFromThing() = ???

  def deleteCertificate() = ???

  def deletePolicy() = ???

}

object IControlRepo {

  def real(config: Config, iot: Iot) = new ControlRepoImpl(config, iot)

}
