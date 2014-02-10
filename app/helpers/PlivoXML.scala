package helpers

import scala.xml._
import scala.collection.mutable.ListBuffer

import models._

import play.api.i18n._
import play.api.Logger

import java.util.LinkedHashMap

import com.plivo.helper.api.client.RestAPI
import com.plivo.helper.exception.PlivoException
import com.plivo.helper.api.response.call.Call

object PlivoXml {

	val validDigits = "12345"

	val digitMap = Map(1 -> "one", 2 -> "two", 3 -> "three", 4 -> "four", 5 -> "five")

	val k1 = ""
	val k2 = ""
	val apiVersion = "v1"
	val phoneNumber = "1234567890"

	def getQuestion(questionInfo: (String, ListBuffer[String]), surveyId: Long, endpoint: String, preamble: String=""): String = {

		val (question, options) = questionInfo
		
		var optList = new ListBuffer[String]
		options.zipWithIndex.foreach { case(opt, pos) => 
			optList += Messages("surveys.ask_question", opt, digitMap.get(pos + 1).getOrElse(""))
		}

		val optString = optList.mkString(" ")
		//val myendpoint = scala.xml.Unparsed(endpoint)

		val ret = 
		<Response>
			<GetDigits action={ endpoint } method="GET" numDigits="1" validDigits={ validDigits }>
				<Speak>
					{ preamble }
					{ question }.
					{ optString }
				</Speak>
			</GetDigits>
		</Response>

		ret.toString

	}

	def makeCall(to: String, answerUrl: String) = {
		val restAPI: RestAPI = new RestAPI(k1, k2, apiVersion)

	    val params: LinkedHashMap[String, String] = new LinkedHashMap[String, String]
	    params.put("from", phoneNumber)
	    params.put("to", to)
	    params.put("answer_url", answerUrl)
	    params.put("answer_method", "GET")

	    var response: Call = null
	    try {
	      response = restAPI.makeCall(params)
	      Logger.info(response.apiId)
	    }
	    catch {
	      case e: PlivoException => {
	        Logger.error(e.getMessage)
	      }
	    }
	}

}