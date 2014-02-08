package helpers

import scala.collection.mutable.ListBuffer

import models._
import com.redis._

object Redis {

	val port = 6379
	val host = "localhost"

	val r = new RedisClient(host, port)

	def getQuestionsKey(surveyId: Long, suffix: String): String = {
		"survey:" + surveyId + ":" + suffix
	}

	def getOptionsKey(surveyId: Long, questionId: Long, suffix: String): String = {
		"survey:" + surveyId + ":" + questionId + ":" + suffix
	}

	def enqueueSurvey(survey: Survey) = {
		
		//set up hash of survey details
		val hashKey = "survey:" + survey.id.get
		r.hset(hashKey, "id", survey.id.get)
		r.hset(hashKey, "name", survey.name)
		r.hset(hashKey, "greeting", survey.greeting)
		r.hset(hashKey, "survey_type", survey.surveyType)
		r.hset(hashKey, "user_id", survey.userId)
		r.hset(hashKey, "created_at", survey.createdAt.toString)

		//set up list of questions and question_ids
		val k1 = getQuestionsKey(survey.id.get, "questions")
		val k2 = getQuestionsKey(survey.id.get, "question_ids")
		Survey.findQuestions(survey.id.get).foreach { question =>
			r.rpush(k1, question.question)
			r.rpush(k2, question.id.get)

			//set up list of options
			val k3 = getOptionsKey(survey.id.get, question.id.get, "options")
			val k4 = getOptionsKey(survey.id.get, question.id.get, "option_ids")
			val k5 = getOptionsKey(survey.id.get, question.id.get, "result")
			SurveyQuestion.findOptions(question.id.get).foreach { option =>
				r.rpush(k3, option.answer)
				r.rpush(k4, option.id.get)
				r.hset(k5, option.id.get, 0)
			}
		}

		//place survey in queue
		r.rpush("survey_ids", survey.id.get)
		
	}

	def dequeueSurvey(survey: Survey) = {
		r.del("survey:" + survey.id.get)
		r.del(getQuestionsKey(survey.id.get, "questions"))
		r.del(getQuestionsKey(survey.id.get, "question_ids"))

		Survey.findQuestions(survey.id.get).foreach { question =>
			r.del(getOptionsKey(survey.id.get, question.id.get, "options"))
			r.del(getOptionsKey(survey.id.get, question.id.get, "option_ids"))
			r.del(getOptionsKey(survey.id.get, question.id.get, "result"))
		}
	}

	def getSurveyResults(survey: Survey): ListBuffer[(Long, Int)] = {
		var ret = new ListBuffer[(Long,Int)]

		Survey.findQuestions(survey.id.get).foreach { question =>
			val k = getOptionsKey(survey.id.get, question.id.get, "result")
			SurveyQuestion.findOptions(question.id.get).foreach { option =>
				val count = r.hget(k, option.id.get).getOrElse("0")
				ret += (option.id.get -> count.toInt)
			}
		}

		ret
	}

	def getNextQuestion(surveyId: Long, position: Int): Option[(String, ListBuffer[String])] = {
		val k1 = getQuestionsKey(surveyId, "questions")
		val k2 = getQuestionsKey(surveyId, "question_ids")
		r.lindex(k1, position).map { question =>
			val questionId = r.lindex(k2, position).getOrElse("-1")

			val options = new ListBuffer[String]
			val k3 = getOptionsKey(surveyId, questionId.toLong, "options")
			r.lrange(k3, 0, -1).map { optionList => 
				optionList.foreach { option =>
					options += option.getOrElse("")
				}
			}

			Some(question -> options)
		}.getOrElse(None)
	}

	def recordAnswer(surveyId: Long, questionPosition: Int, answerPosition: Int) = {
		val k = getQuestionsKey(surveyId, "question_ids")
		r.lindex(k, questionPosition).map { questionId => 
			val k1 = getOptionsKey(surveyId, questionId.toLong, "option_ids")
			r.lindex(k1, answerPosition).map { optionId =>
				val k2 = getOptionsKey(surveyId, questionId.toLong, "result")
				r.hincrby(k2, optionId, 1)
			}
		}
	}

	def getNextSurveyId: Long = {
		r.lpop("survey_ids").map { i =>
			i.toLong
		}.getOrElse(-1L)
	}

	def doIt(): Option[String] = {
		//r.get("mykey")

		//recordAnswer(25L, 0, 0)
		//r.hget("survey:" + 25L + ":" + 56L + ":result", 63.toString)
		//Worker.ping
		Some("hey")
		/*
		getNextQuestion(25L, 0).map { x =>
			val (question, options) = x

			//Some(PlivoXml.getQuestion((question, options), 25L, 1))

			Some(question + " -> " + options.mkString(","))
		}.getOrElse(None)
		*/
	}
}