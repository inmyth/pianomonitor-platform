package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{DeviceId, UserDeviceItem, UserId}
import com.kerahbiru.platform.{Config, ServiceError}
import facade.amazonaws.services.dynamodb.DynamoDB
import monix.eval.Task

abstract class UserDeviceRepoInterface(config: Config) {

  def putDevice(
      userId: UserId,
      item: UserDeviceItem
  ): Task[Either[ServiceError, Unit]]

  def listDevices(userId: UserId): Task[Either[ServiceError, List[UserDeviceItem]]]

  def getDevice(
      userId: UserId,
      deviceId: DeviceId
  ): Task[Either[ServiceError, UserDeviceItem]]

  def deleteDevice(userId: UserId, deviceId: DeviceId): Task[Either[ServiceError, Unit]]

}

object UserDeviceRepoInterface {

  def real(config: Config, db: DynamoDB) = new UserDeviceRepoDynamodb(config, db)

}
