package com.kerahbiru.platform

trait DomainError

case class DynamoDbError(msg: String) extends DomainError
