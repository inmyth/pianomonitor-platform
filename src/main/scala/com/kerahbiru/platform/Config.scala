package com.kerahbiru.platform

import cats.data.Reader
import com.kerahbiru.platform.repo.{ControlRepoImpl, IControlRepo}
import facade.amazonaws.services.dynamodb.DynamoDB
import facade.amazonaws.services.iot.Iot

import scala.scalajs.js

final case class Config(
    thingArn: String,
    thingPrincipal: String,
    thingRegion: String,
    ddbDeviceTable: String
)

object Config {

  def load(): Config = {
    val ddbDeviceTable = js.Dynamic.global.process.env.DDB_DEVICE_TABLE.asInstanceOf[js.UndefOr[String]].toOption.get
    val thingArn       = js.Dynamic.global.process.env.THING_ARN.asInstanceOf[js.UndefOr[String]].toOption.get
    // arn:aws:iot:us-west-2:734435107319:thing/MyWindowsIOT
    val arnEls         = thingArn.split("[:]")
    val thingRegion    = arnEls(3)
    val thingPrincipal = arnEls(4)
    Config(thingArn, thingPrincipal, thingRegion, ddbDeviceTable)
  }

  final case class ControlRepo(config: Config) {
    val iot = new Iot()

    val control = IControlRepo.real(config, iot)
  }

  object ControlRepo {
    val fromConfig: Reader[Config, ControlRepo] = Reader(ControlRepo(_))
  }

  /*
    final case class PublisherConfig(config: Config) {
    val sns = new SNS()

    val pub: Pub = config.envMode match {
      case InMem => Pub.fakeBroker(Config.ec, config)

      case _ => Pub.sns(Config.ec, config, sns)
    }

  }

  object PublisherConfig {
    val fromConfig: Reader[Config, PublisherConfig] = Reader(PublisherConfig(_))
  }
   */
}
