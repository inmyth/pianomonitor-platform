package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{DeviceId, UserDeviceItem}
import com.kerahbiru.platform.{Config, ServiceError}
import facade.amazonaws.services.cognitoidentity.IdentityId
import facade.amazonaws.services.dynamodb.DynamoDB
import monix.eval.Task

abstract class UserDeviceRepoInterface(config: Config) {

  def putDevice(
      identityId: IdentityId,
      item: UserDeviceItem
  ): Task[Either[ServiceError, Unit]]

  def listDevices(identityId: IdentityId): Task[Either[ServiceError, List[UserDeviceItem]]]

  def getDevice(
      identityId: IdentityId,
      deviceId: DeviceId
  ): Task[Either[ServiceError, UserDeviceItem]]

  def deleteDevice(identityId: IdentityId, deviceId: DeviceId): Task[Either[ServiceError, Unit]]

}

object UserDeviceRepoInterface {

  def real(config: Config, db: DynamoDB) = new UserDeviceRepoDynamodb(config, db)

}
