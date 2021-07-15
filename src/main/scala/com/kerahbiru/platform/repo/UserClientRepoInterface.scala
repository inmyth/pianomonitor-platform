package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{ClientId, UserClientItem, UserId}
import com.kerahbiru.platform.{Config, ServiceError}
import facade.amazonaws.services.dynamodb.DynamoDB
import monix.eval.Task

abstract class UserClientRepoInterface(config: Config) {

  def putClient(
      userId: UserId,
      item: UserClientItem
  ): Task[Either[ServiceError, Unit]]

  def listDevices(userId: UserId): Task[Either[ServiceError, List[UserClientItem]]]

  def getClient(
      userId: UserId,
      clientId: ClientId
  ): Task[Either[ServiceError, UserClientItem]]

  def deleteClient(userId: UserId, clientId: ClientId): Task[Either[ServiceError, Unit]]

}

object UserClientRepoInterface {

  def real(config: Config, db: DynamoDB) = new UserClientRepoDynamodb(config, db)

}
