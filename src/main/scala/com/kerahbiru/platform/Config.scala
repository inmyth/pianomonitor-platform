package com.kerahbiru.platform

import cats.data.Reader
import com.kerahbiru.platform.repo.{ThingManagementAwsIot, UserClientRepoInterface, ThingManagementInterface}
import facade.amazonaws.services.dynamodb.DynamoDB
import facade.amazonaws.services.iot.{Iot, ThingArn}

import scala.scalajs.js

final case class Config(
    thingArn: ThingArn,
    thingName: String,
    thingPrincipal: String,
    thingRegion: String,
    ddbDeviceTable: String
)

object Config {

  def load(): Config = {
    val ddbDeviceTable = js.Dynamic.global.process.env.DDB_DEVICE_TABLE.asInstanceOf[js.UndefOr[String]].toOption.get
    val thingArn       = js.Dynamic.global.process.env.THING_ARN.asInstanceOf[js.UndefOr[ThingArn]].toOption.get
    // arn:aws:iot:us-west-2:734435107319:thing/MyWindowsIOT
    val arnEls         = thingArn.split("[:]")
    val arnElsBs       = thingArn.split("[/]")
    val thingRegion    = arnEls(3)
    val thingPrincipal = arnEls(4)
    val thingName      = arnElsBs.last
    Config(thingArn, thingName, thingPrincipal, thingRegion, ddbDeviceTable)
  }

  final case class ThingManagement(config: Config) {
    val iot = new Iot()

    val tm: ThingManagementInterface = ThingManagementInterface.real(config, iot)
  }

  object ThingManagement {
    val fromConfig: Reader[Config, ThingManagement] = Reader(ThingManagement(_))
  }

  final case class UserClientRepo(config: Config) {

    val db = new DynamoDB()

    val repo: UserClientRepoInterface = UserClientRepoInterface.real(config, db)
  }

  object UserClientRepo {

    val fromConfig: Reader[Config, UserClientRepo] = Reader(UserClientRepo(_))

  }

}
