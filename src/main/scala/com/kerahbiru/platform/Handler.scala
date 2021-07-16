package com.kerahbiru.platform

import monix.eval.Task
import monix.execution.Scheduler
import net.exoego.facade.aws_lambda.{APIGatewayProxyEvent, APIGatewayProxyResult, Context}

import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.scalajs.js.annotation.JSExportTopLevel

object Handler {
  implicit val ec: Scheduler = monix.execution.Scheduler.Implicits.global

  @JSExportTopLevel(name = "handler")
  val handler: js.Function2[APIGatewayProxyEvent, Context, js.Promise[APIGatewayProxyResult]] = {
    (event: APIGatewayProxyEvent, _: Context) =>
      (for {
        a <- Task(Config.load())
        b <- Task(Services.fromConfig(a))
        c <- b.process(event)
      } yield c).runToFuture.toJSPromise

  }
}
