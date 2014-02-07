package helpers

import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import scala.collection.mutable.ListBuffer

import java.util.LinkedHashMap
import com.plivo.helper.api.client.RestAPI
import com.plivo.helper.exception.PlivoException
import com.plivo.helper.api.response.call.Call

object Utils {
	
	//session length in hours
	val sessionLength = 5

	val dateFormat = "yyyy-MM-dd HH:mm:ss"


	def getFormattedCurrentTime(): String = {
		getFormattedTime(getCurrentTime())
	}

	def getFormattedTime(d: Date): String = {
		val dateTimeFormat = new SimpleDateFormat(dateFormat)
		dateTimeFormat.format(d)
	}

	def getCurrentTime(): Date = {
		Calendar.getInstance().getTime()
	}

	def getExpiryTime(): Date = {
		val cal = Calendar.getInstance()
		cal.setTime(getCurrentTime())
		cal.add(Calendar.HOUR, sessionLength)
		cal.getTime()
	}

	def getFormattedExpiryTime(): String = {
		getFormattedTime(getExpiryTime())
	}

	// Generate a random string of length n from the given alphabet
	def randomString(alphabet: String)(n: Int): String = {
	  val random = new scala.util.Random(new java.security.SecureRandom())
	  Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString
	}
 
    // Generate a random alphabnumeric string of length nextInt
    def randomAlphanumericString(n: Int) = 
      randomString("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")(n)

    def getErrorMessage(errors: ListBuffer[String]): String = {
    	errors.mkString(", ")
    }

    def testCall = {
    	val restAPI: RestAPI = new RestAPI("foo", "bar", "v1")

	    val params: LinkedHashMap[String, String] = new LinkedHashMap[String, String]
	    params.put("from", "1234567890")
	    params.put("to", "1234567890")
	    params.put("answer_url", "http://example.com")

	    var response: Call = null
	    try {
	      response = restAPI.makeCall(params)
	      System.out.println(response.apiId)
	    }
	    catch {
	      case e: PlivoException => {
	        System.out.println(e.getMessage)
	      }
	    }
    }

}

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class PageVars(orderBy: String, order: String, filter: String)
