package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Config
import com.kerahbiru.platform.Entities.{ClientId, Device, UserId}
import monix.eval.Task
import monix.execution.Scheduler

abstract class DeviceRepo(config: Config) {

  def putDevice(userId: UserId): Task[Unit]

  def listDevices(userId: UserId): Task[List[DeviceRepo]]

  def deleteDevice(clientId: ClientId): Task[Unit]

}
