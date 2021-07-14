package com.kerahbiru.platform

import scala.scalajs.js

final case class Config(
    ddbDeviceTable: String,
    thingArn: String
)

object Config {

  def load(): Config = {
    val ddbDeviceTable = js.Dynamic.global.process.env.DDB_DEVICE_TABLE.asInstanceOf[js.UndefOr[String]].toOption.get
    val thingArn       = js.Dynamic.global.process.env.THING_ARN.asInstanceOf[js.UndefOr[String]].toOption.get
    Config(ddbDeviceTable, thingArn)
  }
}
