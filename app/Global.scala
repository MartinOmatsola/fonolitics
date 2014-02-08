import play.api._
import play.api.libs.concurrent._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import akka.actor._

import scala.concurrent.duration._

import models.Worker
import models.ProcessOneSurvey

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    Akka.system.scheduler.schedule(
      0 seconds,
      5 seconds,
      Worker.default,
      ProcessOneSurvey()
      )
  }


}