package com.kerahbiru.platform

sealed abstract class ServiceError(val msg: String, val level: Level)

case class DynamoDbError(m: String) extends ServiceError(m, SERVER)
case class IotError(m: String)      extends ServiceError(m, SERVER)
case class ApiError(m: String)      extends ServiceError(m, SERVER)

sealed trait Level
case object SERVER extends Level
case object USER   extends Level
