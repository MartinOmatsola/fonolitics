package helpers

import scala.collection.mutable.ListBuffer

import models._
import com.redis._

object Redis {

	val port = 6379
	val host = "localhost"

	val r = new RedisClient(host, port)

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
		val k1 = "survey:" + survey.id.get + ":questions"
		val k2 = "survey:" + survey.id.get + ":question_ids"
		Survey.findQuestions(survey.id.get).foreach { question =>
			r.rpush(k1, question.question)
			r.rpush(k2, question.id.get)

			//set up list of options
			val k3 = "survey:" + survey.id.get + ":" + question.id.get + ":options"
			val k4 = "survey:" + survey.id.get + ":" + question.id.get + ":option_ids"
			val k5 = "survey:" + survey.id.get + ":" + question.id.get + ":result"
			SurveyQuestion.findOptions(question.id.get).foreach { option =>
				r.rpush(k3, option.answer)
				r.rpush(k4, option.id.get)
				r.hset(k5, option.id.get, 0)
			}
		}

		//place survey in queue
		r.rpush("survey_ids", survey.id.get)
		
	}

	def getNextQuestion(surveyId: Long, position: Int): Option[(String, ListBuffer[String])] = {
		val k1 = "survey:" + surveyId + ":questions"
		val k2 = "survey:" + surveyId + ":question_ids"
		r.lindex(k1, position).map { question =>
			val questionId = r.lindex(k2, position).getOrElse(-1)

			val options = new ListBuffer[String]
			val k3 = "survey:" + surveyId + ":" + questionId + ":options"
			r.lrange(k3, 0, -1).map { optionList => 
				optionList.foreach { option =>
					options += option.getOrElse("")
				}
			}

			Some(question -> options)
		}.getOrElse(None)
	}

	def recordAnswer(surveyId: Long, questionId: Long, answerPosition: Int) = {
		val k1 = "survey:" + surveyId + ":" + questionId + ":option_ids"
		r.lindex(k1, answerPosition).map { optionId =>
			val k2 = "survey:" + surveyId + ":" + questionId + ":result"
			r.hincrby(k2, optionId, 1)
		}
	}

	def doIt(): Option[String] = {
		//r.get("mykey")

		recordAnswer(25L, 56L, 0)
		r.hget("survey:" + 25L + ":" + 56L + ":result", 63.toString)

		/*
		getNextQuestion(25L, 2).map { x =>
			val (question, options) = x
			Some(question + " -> " + options.mkString(","))
		}.getOrElse(None)
		*/
	}
}