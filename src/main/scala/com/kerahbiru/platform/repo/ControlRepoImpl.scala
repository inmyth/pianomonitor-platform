package com.kerahbiru.platform.repo

import com.kerahbiru.platform.Entities.{ClientId, UserId}
import com.kerahbiru.platform.repo.ControlRepoImpl.createPolicyJson
import com.kerahbiru.platform.{Config, DomainError, Entities, IotError}
import facade.amazonaws.services.iot.{CreatePolicyRequest, Iot, PolicyArn}
import monix.eval.Task

import scala.scalajs.js
import scala.scalajs.js.UndefOr

class ControlRepoImpl(config: Config, iot: Iot) extends IControlRepo(config) {

  override def createPolicy(
      userId: Entities.UserId,
      clientId: Entities.ClientId
  ): Task[Either[DomainError, PolicyArn]] =
    Task
      .fromFuture {
        iot.createPolicyFuture(
          CreatePolicyRequest(
            policyDocument = createPolicyJson(config.thingPrincipal, config.thingRegion, userId, clientId),
            policyName = s"${userId.value}_${clientId.value}"
          )
        )
      }
      .map(p =>
        p.policyArn.toOption match {
          case Some(value) => Right(value)
          case None        => Left(IotError("Policy creation response empty"))

        }
      )
      .onErrorHandle(e => Left(IotError(e.getMessage)))

}

object ControlRepoImpl {

  def createPolicyJson(principal: String, region: String, userId: UserId, clientId: ClientId): String =
    s"""{
      |"Version": "2012-10-17",
      |"Statement": [
      |  {
      |    "Effect": "Allow",
      |    "Action": "iot:Subscribe",
      |    "Resource": "arn:aws:iot:$region:$principal:topicfilter/${userId.value}/${clientId.value}"
      |  },
      |  {
      |    "Effect": "Allow",
      |    "Action": "iot:Connect",
      |    "Resource": "arn:aws:iot:$region:$principal:client/${clientId.value}"
      |  },
      |  {
      |    "Effect": "Allow",
      |    "Action": "iot:Publish",
      |    "Resource": "arn:aws:iot:${region}:${principal}:topic/${clientId.value}"
      |  }
      |]
      |}""".stripMargin

}
