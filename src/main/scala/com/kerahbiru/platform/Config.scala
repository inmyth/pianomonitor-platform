package com.kerahbiru.platform

import facade.amazonaws.services.dynamodb.DynamoDB

import scala.scalajs.js

final case class Config(
    ddbDeviceTable: String,
    thingArn: String,
    ddb: DynamoDB
)

object Config {

  def load(): Config = {
    val ddbDeviceTable = js.Dynamic.global.process.env.DDB_DEVICE_TABLE.asInstanceOf[js.UndefOr[String]].toOption.get
    val thingArn       = js.Dynamic.global.process.env.THING_ARN.asInstanceOf[js.UndefOr[String]].toOption.get
    val ddb            = new DynamoDB()
    Config(ddbDeviceTable, thingArn, ddb)
  }
}
