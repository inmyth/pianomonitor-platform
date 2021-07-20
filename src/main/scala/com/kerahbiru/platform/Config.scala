package com.kerahbiru.platform

import cats.data.Reader
import com.kerahbiru.platform.repo.{
  IdentityManagementCognito,
  IdentityManagementInterface,
  ThingManagementInterface,
  UserDeviceRepoInterface
}
import facade.amazonaws.services.cognitoidentity.CognitoIdentity
import facade.amazonaws.services.dynamodb.DynamoDB
import facade.amazonaws.services.iot.{CognitoIdentityPoolId, Iot, ThingArn}

import scala.scalajs.js

final case class Config(
    thingArn: ThingArn,
    thingName: String,
    awsAccount: String,
    region: String,
    ddbDeviceTable: String,
    cognitoUserPoolId: String,
    cognitoIdentityPoolId: CognitoIdentityPoolId
)

object Config {

  def load(): Config = {
    val ddbDeviceTable = js.Dynamic.global.process.env.DDB_DEVICE_TABLE.asInstanceOf[js.UndefOr[String]].toOption.get
    val thingArn       = js.Dynamic.global.process.env.THING_ARN.asInstanceOf[js.UndefOr[ThingArn]].toOption.get
    val arnEls         = thingArn.split("[:]")
    val arnElsBs       = thingArn.split("[/]")
    val region         = arnEls(3)
    val account        = arnEls(4)
    val thingName      = arnElsBs.last
    val cognitoUserPoolId =
      js.Dynamic.global.process.env.COGNITO_USER_POOL_ID.asInstanceOf[js.UndefOr[String]].toOption.get
    val cognitoIdentityPoolId =
      js.Dynamic.global.process.env.COGNITO_IDENTITY_POOL_ID
        .asInstanceOf[js.UndefOr[CognitoIdentityPoolId]]
        .toOption
        .get
    Config(thingArn, thingName, account, region, ddbDeviceTable, cognitoUserPoolId, cognitoIdentityPoolId)
  }

  final case class ThingManagement(config: Config) {
    val iot = new Iot()

    val tm: ThingManagementInterface = ThingManagementInterface.real(config, iot)
  }

  object ThingManagement {
    val fromConfig: Reader[Config, ThingManagement] = Reader(ThingManagement(_))
  }

  final case class UserDeviceRepo(config: Config) {

    val db = new DynamoDB()

    val repo: UserDeviceRepoInterface = UserDeviceRepoInterface.real(config, db)
  }

  object UserDeviceRepo {

    val fromConfig: Reader[Config, UserDeviceRepo] = Reader(UserDeviceRepo(_))

  }

  final case class IdentityManagement(config: Config) {
    val ci = new CognitoIdentity()

    val im: IdentityManagementCognito = IdentityManagementInterface.real(config, ci)
  }

  object IdentityManagement {
    val fromConfig: Reader[Config, IdentityManagement] = Reader(IdentityManagement(_))

  }
}
