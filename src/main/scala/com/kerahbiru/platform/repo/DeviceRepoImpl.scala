package com.kerahbiru.platform.repo

import com.kerahbiru.platform.{Config, Entities}
import monix.eval.Task
import monix.execution.Scheduler

class DeviceRepoImpl(config: Config) extends DeviceRepo(config) {

  override def putDevice(userId: Entities.UserId): Task[Unit] = ???

  override def listDevices(userId: Entities.UserId): Task[List[DeviceRepo]] = ???

  override def deleteDevice(clientId: Entities.ClientId): Task[Unit] = ???
}
