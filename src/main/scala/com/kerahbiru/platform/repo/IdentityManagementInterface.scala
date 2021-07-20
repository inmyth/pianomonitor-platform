package com.kerahbiru.platform.repo

import com.kerahbiru.platform.{Config, ServiceError}
import facade.amazonaws.services.cognitoidentity.{CognitoIdentity, IdentityId}
import monix.eval.Task

abstract class IdentityManagementInterface(config: Config) {

  def getIdentityId(jwtToken: String): Task[Either[ServiceError, IdentityId]]

}

object IdentityManagementInterface {

  def real(config: Config, ci: CognitoIdentity) = new IdentityManagementCognito(config, ci)

}
