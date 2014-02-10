package models

import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import helpers._
import models._

object Worker {
  
  implicit val timeout = Timeout(1 second)
  
  val default = Akka.system.actorOf(Props[Worker])
  
}

class Worker extends Actor {

  def receive = {
  
    case ProcessOneSurvey() => {
      val surveyId = Redis.getNextSurveyId
      if (surveyId > 0) {
        Survey.findById(surveyId).map { survey =>
          val domain = Play.current.configuration.getString("my.domain").getOrElse("")
          val endpoint = domain + controllers.routes.Calls.answer(surveyId, 0)
          Logger.info(endpoint)
          PlivoXml.makeCall("1234567890", endpoint)
        }
      }
    }
    
  }
  
}

case class ProcessOneSurvey()
