package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{CertCreationResponse, ClientId, UserId}
import com.kerahbiru.platform.{Config, ServiceError}
import facade.amazonaws.services.dynamodb.DynamoDB
import facade.amazonaws.services.iot.{CertificateArn, PolicyName}
import monix.eval.Task

abstract class UserClientRepoInterface(config: Config) {

  def putDevice(
      userId: UserId,
      clientId: ClientId,
      certificateArn: CertificateArn,
      policyName: PolicyName
  ): Task[Either[ServiceError, Unit]]

//  def listDevices(userId: UserId): Task[Either[ServiceError, List[Device]]]

  def deleteDevice(userId: UserId, clientId: ClientId): Task[Either[ServiceError, Unit]]

}

object UserClientRepoInterface {

  def real(config: Config, db: DynamoDB) = new UserClientRepoDynamodb(config, db)

}
