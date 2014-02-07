package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.i18n._

import anorm._

import models._
import views._
import helpers._

object Surveys extends Controller with Secured {

  val Home = Redirect(routes.Surveys.list())

  def list(page: Int, orderBy: String, order: String, filter: String) = withUser { user => implicit request =>  
    Ok(html.surveys.list(user,
      Survey.findInvolving(user.id.get, page = page, orderBy = orderBy, order = order, 
      						filter = ("%"+filter+"%")),
      PageVars(orderBy, order, filter)
    ))
  }

  def save = withUser { user => implicit request =>
    val (survey, error) = getSurveyFromRequest(request, user)
    if (error == "") {
      val id = Survey.insert(survey)
      Redirect(routes.Surveys.edit(id)).flashing("success" -> Messages("surveys.create_success", survey.name))
    } else {
      Ok(html.surveys.edit(user, None, survey, Some(error)))
    }
  }

  def edit(id: Long) = withUser { user => implicit request =>
    Survey.findById(id).map { survey =>
      Ok(html.surveys.edit(user, Some(id), survey))
    }.getOrElse(NotFound)
  }
  
  def getSurveyFromRequest(req: Request[AnyContent], user: User): (Survey, String) = {
    req.body.asFormUrlEncoded.map { params =>
      val pName = params.get("name").get.head
      val pGreeting = params.get("greeting").get.head

      val errors = new scala.collection.mutable.ListBuffer[String]

      if (pName.isEmpty)     errors += Messages("general.required_field", Messages("surveys.Name"))
      if (pGreeting.isEmpty) errors += Messages("general.required_field", Messages("surveys.Greeting"))
      
      val survey = Survey(userId=user.id.get, email=user.email, name=pName, greeting=pGreeting)

      if (errors.isEmpty) (survey, "")
      else (survey, Utils.getErrorMessage(errors))
    }.getOrElse(Survey(), "")
  }

  def update(id: Long) = withUser { user => implicit request =>
    val (survey, error) = getSurveyFromRequest(request, user)
    if (error == "") {
      Survey.update(id, survey)
      Redirect(routes.Surveys.edit(id)).flashing("success" -> Messages("surveys.update_success", survey.name))
    } else {
      Ok(html.surveys.edit(user, Some(id), survey, Some(error)))
    }
  }

  def create = withUser { user => implicit request =>
    Ok(html.surveys.edit(user, None, Survey(userId=user.id.get, email=user.email)))
  }

  def delete(id: Long) = withUser { user => implicit request =>
    val name = Survey.findById(id).map { survey => survey.name }.getOrElse("")
    Survey.delete(id)
    Redirect(routes.Surveys.list()).flashing("success" -> Messages("surveys.delete_success", name))
  }

  def questions(id: Long) = withUser { user => implicit request =>
    
    Survey.findById(id).map { survey =>
      val questions = Survey.findQuestions(survey.id.get)
      if (!questions.isEmpty) {
        
        var m = new scala.collection.mutable.ListBuffer[(SurveyQuestion, Option[Seq[SurveyQuestionOption]])]
        
        questions.foreach { question =>
          val options = SurveyQuestion.findOptions(question.id.get)
          if (!options.isEmpty) {
            m += (question -> Some(options))
          } else {
            m += (question -> None)
          }
        }
        
        Ok(html.surveys.questions(user, survey, Some(m)))

      } else {
        Ok(html.surveys.questions(user, survey, None))
      }
    }.getOrElse(NotFound)

  }

  def saveQuestions(id: Long) = withUser { user => implicit request =>
    
    Survey.findById(id).map { survey =>
      request.body.asFormUrlEncoded.map { params =>
        
        val questionIds = params.get("question_ids").getOrElse(List()) 

        var index = 0L
        params.get("questions").get.foreach { q => 
          
          if (!q.isEmpty) {
            //save question
            val time = Some(new java.util.Date())
            var qId = 0L
            
            //check if it has an id, if it does update it, else do insert
            if (questionIds.length > index) {
              
              val sq = SurveyQuestion(surveyId=survey.id.get, survey=survey.name, question=q,
                                      sequenceOrder=index, modifiedBy=Some(user.id.get), modifiedAt=time,
                                      modifiedEmail=Some(user.email))

              qId = questionIds(index.toInt).toLong

              SurveyQuestion.update(qId, sq)

            } else {
            
              val sq = SurveyQuestion(surveyId=survey.id.get, survey=survey.name, question=q,
                                        sequenceOrder=index, createdBy=Some(user.id.get), createdAt=time,
                                        createdEmail=Some(user.email), modifiedBy=Some(user.id.get), modifiedAt=time,
                                        modifiedEmail=Some(user.email))


              qId = SurveyQuestion.insert(sq)

            }

            val optionIds = params.get("question" + (index+1) + "_option_ids").getOrElse(List())

            //save options
            var j = 0L
            params.get("question" + (index+1) + "_options").get.foreach { opt =>

              if (!opt.isEmpty) {
                //check if it has an id, if it does update it, else do insert  
                if (optionIds.length > j) {

                  val sqo = SurveyQuestionOption(surveyQuestionId=qId, surveyQuestion=q, answer=opt, sequenceOrder=j,
                                                modifiedBy=Some(user.id.get), modifiedAt=time, modifiedEmail=Some(user.email))

                  val sqoId = optionIds(j.toInt).toLong

                  SurveyQuestionOption.update(sqoId, sqo)

                } else {

                  val sqo = SurveyQuestionOption(surveyQuestionId=qId, surveyQuestion=q, answer=opt, sequenceOrder=j,
                                                  createdAt=time, createdEmail=Some(user.email), modifiedBy=Some(user.id.get),
                                                  modifiedAt=time, modifiedEmail=Some(user.email))

                  SurveyQuestionOption.insert(sqo)

                }

              } else {
                //check if it has an id, if it does delete it
                if (optionIds.length > j) SurveyQuestionOption.delete(optionIds(j.toInt).toLong)
              }

              j += 1

            }

            index += 1

          } else {
            //check if it has an id, if it does delete it
            if (questionIds.length > index) SurveyQuestion.delete(questionIds(index.toInt).toLong)

          }

        }

      }

      //Ok(request.body.asFormUrlEncoded.toString())
      
      Redirect(routes.Surveys.questions(id)).flashing("success" -> Messages("surveys.update_success", survey.name))

    }.getOrElse(NotFound)

  }

  def launch(id: Long) = withUser { user => implicit request =>
    Survey.findById(id).map { survey =>
      //Redis.enqueueSurvey(survey)
      Utils.testCall
      Ok(Redis.doIt().toString)
    }.getOrElse(NotFound)
  }
  
}