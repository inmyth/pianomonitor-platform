package com.kerahbiru.platform

sealed abstract class ServiceError(val msg: String)

case class DynamoDbError(m: String) extends ServiceError(m)
case class IotError(m: String)      extends ServiceError(m)
