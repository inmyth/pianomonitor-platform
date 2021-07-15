package com.kerahbiru.platform

trait DomainError

case class DynamoDbError(msg: String) extends DomainError
case class IotError(msg: String)      extends DomainError
