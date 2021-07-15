package com.kerahbiru.platform

import cats.data.Reader
import com.kerahbiru.platform.Config.ControlRepo
import com.kerahbiru.platform.Entities.{ClientId, Device, UserId}
import com.kerahbiru.platform.repo.ControlRepoImpl
import monix.eval.Task

import java.util.UUID
import scala.scalajs.js

case class Services(controlRepo: ControlRepo) {

  def createDevice(): Device = ???

  private def newUUID(): UUID = UUID.randomUUID()

  def process(): Task[js.Object] =
    for {
      a <- controlRepo.control.createPolicy(UserId("martin"), ClientId.generate)
    } yield new js.Object(a)

}

object Services {

  val fromConfig: Reader[Config, Services] = for {
    c <- ControlRepo.fromConfig
  } yield Services(c)

  def start(services: Services): Task[js.Object] = services.process()
}
