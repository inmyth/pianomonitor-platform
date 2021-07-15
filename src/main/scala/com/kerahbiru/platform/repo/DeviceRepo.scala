package com.kerahbiru.platform.repo

import com.kerahbiru.platform.{Config, DomainError}
import com.kerahbiru.platform.Entities.{ClientId, Device, UserId}
import monix.eval.Task
import monix.execution.Scheduler

abstract class DeviceRepo(config: Config) {

  def putDevice(userId: UserId, device: Device): Task[Either[DomainError, Unit]]

  def listDevices(userId: UserId): Task[Either[DomainError, List[Device]]]

  def deleteDevice(userId: UserId, clientId: ClientId): Task[Either[DomainError, Unit]]

}
