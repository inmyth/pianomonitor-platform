package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{DeviceId, DeviceName, UserDeviceItem, UserId}
import com.kerahbiru.platform._
import facade.amazonaws.services.dynamodb.{
  AttributeMap,
  AttributeValue,
  DeleteItemInput,
  DynamoDB,
  GetItemInput,
  PutItemInput,
  QueryInput,
  QueryOutput
}
import monix.eval.Task

import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.Dictionary

class UserDeviceRepoDynamodb(config: Config, db: DynamoDB) extends UserDeviceRepoInterface(config) {

  val table: String = config.ddbDeviceTable

  override def putDevice(
      userId: Entities.UserId,
      item: UserDeviceItem
  ): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        db.putItemFuture(
          PutItemInput(
            Item = Dictionary(
              "userId"         -> AttributeValue.S(userId.value),
              "deviceId"       -> AttributeValue.S(item.deviceId.value),
              "creationTs"     -> AttributeValue.NFromLong(item.creationTs),
              "deviceName"     -> AttributeValue.S(item.deviceName.value),
              "policyName"     -> AttributeValue.S(item.policyName),
              "certificateArn" -> AttributeValue.S(item.certificateArn)
            ),
            TableName = table
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(DynamoDbError(e.getMessage)))

  override def deleteDevice(userId: Entities.UserId, deviceId: Entities.DeviceId): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        db.deleteItemFuture(
          DeleteItemInput(
            Key = Dictionary(
              "userId"   -> AttributeValue.S(userId.value),
              "deviceId" -> AttributeValue.S(deviceId.value)
            ),
            TableName = table
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle {
        case e if e.getMessage.contains("ResourceNotFoundException") => Right(())
        case e                                                       => Left(DynamoDbError(e.getMessage))
      }

  val mapper: AttributeMap => UserDeviceItem = a => {
    val deviceId       = DeviceId(a.get("deviceId").get.S.get)
    val deviceName     = DeviceName(a.get("deviceName").get.S.get)
    val creationTs     = a.get("creationTs").get.N.get.toLong
    val policyName     = a.get("policyName").get.S.get
    val certificateArn = a.get("certificateArn").get.S.get
    UserDeviceItem(deviceId, deviceName, creationTs, certificateArn, policyName)
  }

  override def getDevice(userId: Entities.UserId, deviceId: DeviceId): Task[Either[ServiceError, UserDeviceItem]] =
    Task
      .fromFuture(
        db.getItemFuture(
          GetItemInput(
            Key = Dictionary(
              "userId"   -> AttributeValue.S(userId.value),
              "deviceId" -> AttributeValue.S(deviceId.value)
            ),
            TableName = table
          )
        )
      )
      .map(p =>
        p.Item.toOption match {
          case Some(value) => Right(mapper.apply(value))
          case None        => Left(ValidationError("Item not found"))
        }
      )
      .onErrorHandle(e => Left(DynamoDbError(e.getMessage)))

  override def listDevices(userId: Entities.UserId): Task[Either[ServiceError, List[UserDeviceItem]]] =
    accumulate(userId, ListBuffer.empty, createLoadTask(userId, None))
      .map(p => Right(p.toList))
      .onErrorHandle(e => Left(DynamoDbError(e.getMessage)))

  def createLoadTask(
      userId: UserId,
      startKey: Option[facade.amazonaws.services.dynamodb.Key]
  ): Task[QueryOutput] =
    Task
      .fromFuture(
        db.queryFuture(
          QueryInput(
            TableName = table,
            KeyConditionExpression = "userId = :v1",
            ExpressionAttributeValues = Dictionary(
              ":v1" -> AttributeValue.S(userId.value)
            ),
            ExclusiveStartKey = if (startKey.isEmpty) js.undefined else startKey.get
          )
        )
      )

  def accumulate(
      userId: UserId,
      stack: ListBuffer[UserDeviceItem],
      source: Task[QueryOutput]
  ): Task[ListBuffer[UserDeviceItem]] =
    source.flatMap(p => {
      val lastKey = p.LastEvaluatedKey.toOption
      val events  = p.Items.get.map(mapper)
      stack ++= events
      if (lastKey.isEmpty) {
        Task.now(stack)
      } else {
        accumulate(userId, stack, createLoadTask(userId, lastKey))
      }
    })

}
