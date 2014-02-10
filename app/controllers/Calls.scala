package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.i18n._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play

import anorm._

import models._
import views._
import helpers._

object Calls extends Controller {

	def answer(surveyId: Long, position: Int) = Action.async { implicit request =>
		val ret = scala.concurrent.Future {
			
			//record answer from previous question
			request.getQueryString("Digits").map { answerPosition =>
				Redis.recordAnswer(surveyId, position - 1, answerPosition.toInt - 1)
			}

			val questionInfo = Redis.getNextQuestion(surveyId, position)
			if (!questionInfo.isDefined) {
				//TODO: get exit message from redis
				"<Response><Speak>survey is over</Speak></Response>"
			} else {
				var preamble = ""
				if (position == 0) {
					preamble = Redis.getSurveyDetails(surveyId).get("greeting").getOrElse("") + "."
				}

				val (question, options) = questionInfo.get
				val domain = Play.current.configuration.getString("my.domain").getOrElse("")
				val endpoint = domain + controllers.routes.Calls.answer(surveyId, position + 1) 
				PlivoXml.getQuestion((question, options), surveyId, endpoint, preamble)
			}

		}

		ret.map { Ok(_) }
	}

}