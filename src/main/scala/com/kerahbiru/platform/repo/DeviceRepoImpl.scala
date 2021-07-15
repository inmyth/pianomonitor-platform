package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{ClientId, Device, Topic, UserId}
import com.kerahbiru.platform.{Config, DomainError, DynamoDbError, Entities}
import facade.amazonaws.services.dynamodb.{
  AttributeValue,
  DeleteItemInput,
  DynamoDB,
  PutItemInput,
  QueryInput,
  QueryOutput
}
import monix.eval.Task

import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.Dictionary

class DeviceRepoImpl(config: Config) extends DeviceRepo(config) {

//  val db: DynamoDB  = config.ddb
  val table: String = config.ddbDeviceTable

//  override def deleteDevice(userId: UserId, clientId: Entities.ClientId): Task[Either[DomainError, Unit]] =
//    Task
//      .fromFuture(
//        db.deleteItemFuture(
//          DeleteItemInput(
//            Key = Dictionary(
//              "userId"   -> AttributeValue.S(userId.value),
//              "clientId" -> AttributeValue.S(clientId.value)
//            ),
//            TableName = table
//          )
//        )
//      )
//      .map(_ => Right(()))
//      .onErrorHandle {
//        case e if e.getMessage.contains("ResourceNotFoundException") => Right(())
//        case e                                                       => Left(DynamoDbError(e.getMessage))
//      }
//
//  override def putDevice(userId: Entities.UserId, device: Device): Task[Either[DomainError, Unit]] =
//    Task
//      .fromFuture(
//        db.putItemFuture(
//          PutItemInput(
//            Item = Dictionary(
//              "userId"         -> AttributeValue.S(userId.value),
//              "clientId"       -> AttributeValue.S(device.clientId.value),
//              "policyArn"      -> AttributeValue.S(device.policyArn.value),
//              "policyName"     -> AttributeValue.S(device.policyName.value),
//              "certificateArn" -> AttributeValue.S(device.certificateArn.value),
////              "topic"          -> AttributeValue.S(device.topic.value)
//            ),
//            TableName = table
//          )
//        )
//      )
//      .map(_ => Right(()))
//      .onErrorHandle(e => Left(DynamoDbError(e.getMessage)))
//
//  override def listDevices(userId: Entities.UserId): Task[Either[DomainError, List[Device]]] =
//    accumulate(userId, ListBuffer.empty, createLoadTask(userId, None))
//      .map(p => { Right(p.toList) })
//      .onErrorHandle(e => Left(DynamoDbError(e.getMessage)))
//
//  def createLoadTask(
//      userId: UserId,
//      startKey: Option[facade.amazonaws.services.dynamodb.Key]
//  ): Task[QueryOutput] =
//    Task
//      .fromFuture(
//        db.queryFuture(
//          QueryInput(
//            TableName = table,
//            KeyConditionExpression = "id = :v1",
//            ExpressionAttributeValues = Dictionary(
//              ":v1" -> AttributeValue.S(userId.value)
//            ),
//            ExclusiveStartKey = if (startKey.isEmpty) js.undefined else startKey.get
//          )
//        )
//      )
//
//  def accumulate(
//      userId: UserId,
//      accum: ListBuffer[Device],
//      source: Task[QueryOutput]
//  ): Task[ListBuffer[Device]] =
//    source.flatMap(p => {
//      val lastKey = p.LastEvaluatedKey.toOption
//      val events = p.Items.get.map(q => {
//        val clientId       = ClientId(q.get("clientId").get.S.get)
//        val policyArn      = PolicyArn(q.get("policyArn").get.S.get)
//        val policyName     = PolicyName(q.get("policyName").get.S.get)
//        val certificateArn = CertificateArn(q.get("certificateArn").get.S.get)
//        val topic          = Topic(q.get("topic").get.S.get)
//        Device(clientId, certificateArn, policyArn, policyName, topic)
//      })
//      accum ++= events
//      if (lastKey.isEmpty) {
//        Task.now(accum)
//      } else {
//        accumulate(userId, accum, createLoadTask(userId, lastKey))
//      }
//    })
  override def putDevice(userId: UserId, device: Device): Task[Either[DomainError, Unit]] = ???

  override def listDevices(userId: UserId): Task[Either[DomainError, List[Device]]] = ???

  override def deleteDevice(userId: UserId, clientId: ClientId): Task[Either[DomainError, Unit]] = ???
}
