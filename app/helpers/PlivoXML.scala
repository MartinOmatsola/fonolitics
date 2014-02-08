package helpers

import scala.xml._
import scala.collection.mutable.ListBuffer

import models._

import play.api.i18n._

object PlivoXml {

	val validDigits = "12345"

	val digitMap = Map(1 -> "one", 2 -> "two", 3 -> "three", 4 -> "four", 5 -> "five")

	def getQuestion(questionInfo: (String, ListBuffer[String]), surveyId: Long, endpoint: String=""): String = {

		val (question, options) = questionInfo
		
		var optList = new ListBuffer[String]
		options.zipWithIndex.foreach { case(opt, pos) => 
			optList += Messages("surveys.ask_question", opt, digitMap.get(pos + 1).getOrElse(""))
		}

		val optString = optList.mkString(" ")

		val ret = 
		<Response>
			<GetDigits action={ endpoint } method="GET" numDigits="1" validDigits={ validDigits }>
				<Speak>
					{ question }.
					{ optString }
				</Speak>
			</GetDigits>
		</Response>

		ret.toString

	}

}