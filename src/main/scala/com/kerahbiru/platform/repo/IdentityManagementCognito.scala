package com.kerahbiru.platform.repo

import com.kerahbiru.platform.{CognitoError, Config, ServiceError}
import facade.amazonaws.services.cognitoidentity.{CognitoIdentity, GetIdInput, IdentityId}
import monix.eval.Task

import scala.scalajs.js

class IdentityManagementCognito(config: Config, ci: CognitoIdentity) extends IdentityManagementInterface(config) {

  override def getIdentityId(jwtToken: String): Task[Either[ServiceError, IdentityId]] =
    Task
      .fromFuture(
        ci.getIdFuture(
          GetIdInput(
            IdentityPoolId = config.cognitoIdentityPoolId,
            AccountId = config.awsAccount,
            // `cognito-idp.${AWS_DEFAULT_REGION}.amazonaws.com/${USER_POOL_ID}`
            Logins = js.Dictionary(
              s"cognito-idp.${config.region}.amazonaws.com/${config.cognitoUserPoolId}" -> jwtToken
            )
          )
        )
      )
      .map(p =>
        p.IdentityId.toOption match {
          case Some(value) => Right(value)
          case None        => Left(CognitoError(s"User not found in identity pool: $jwtToken"))
        }
      )
      .onErrorHandle(p => Left(CognitoError(p.getMessage)))
}
