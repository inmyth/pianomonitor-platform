package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{ClientId, UserClientItem, UserId}
import com.kerahbiru.platform.{Config, DynamoDbError, Entities, ServiceError}
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

class UserClientRepoDynamodb(config: Config, db: DynamoDB) extends UserClientRepoInterface(config) {

//  val db: DynamoDB  = config.ddb
  val table: String = config.ddbDeviceTable

  override def putClient(
      userId: Entities.UserId,
      item: UserClientItem
  ): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        db.putItemFuture(
          PutItemInput(
            Item = Dictionary(
              "userId"         -> AttributeValue.S(userId.value),
              "clientId"       -> AttributeValue.S(item.clientId.value),
              "policyName"     -> AttributeValue.S(item.policyName),
              "certificateArn" -> AttributeValue.S(item.certificateArn)
            ),
            TableName = table
          )
        )
      )
      .map(_ => Right(()))
      .onErrorHandle(e => Left(DynamoDbError(e.getMessage)))

  override def deleteClient(userId: Entities.UserId, clientId: Entities.ClientId): Task[Either[ServiceError, Unit]] =
    Task
      .fromFuture(
        db.deleteItemFuture(
          DeleteItemInput(
            Key = Dictionary(
              "userId"   -> AttributeValue.S(userId.value),
              "clientId" -> AttributeValue.S(clientId.value)
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

  val mapper: AttributeMap => UserClientItem = a => {
    val clientId       = ClientId(a.get("clientId").get.S.get)
    val policyName     = a.get("policyName").get.S.get
    val certificateArn = a.get("certificateArn").get.S.get
    UserClientItem(clientId, certificateArn, policyName)
  }

  override def getClient(userId: Entities.UserId, clientId: ClientId): Task[Either[ServiceError, UserClientItem]] =
    Task
      .fromFuture(
        db.getItemFuture(
          GetItemInput(
            Key = Dictionary(
              "userId"   -> AttributeValue.S(userId.value),
              "clientId" -> AttributeValue.S(clientId.value)
            ),
            TableName = table
          )
        )
      )
      .map(p =>
        p.Item.toOption match {
          case Some(value) => Right(mapper.apply(value))
          case None        => Left(DynamoDbError("Item not found"))
        }
      )
      .onErrorHandle(e => Left(DynamoDbError(e.getMessage)))

  override def listDevices(userId: Entities.UserId): Task[Either[ServiceError, List[UserClientItem]]] =
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
      stack: ListBuffer[UserClientItem],
      source: Task[QueryOutput]
  ): Task[ListBuffer[UserClientItem]] =
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
