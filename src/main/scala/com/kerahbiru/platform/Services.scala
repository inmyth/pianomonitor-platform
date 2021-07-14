package com.kerahbiru.platform

import com.kerahbiru.platform.Entities.Device
import com.kerahbiru.platform.repo.DeviceRepo

import java.util.UUID

class Services(deviceRepo: DeviceRepo) {

  def createDevice(): Device = ???

  private def newUUID(): UUID = UUID.randomUUID()

}
