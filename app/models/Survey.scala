package models

import play.api.db._
import play.api.Play.current
import play.api.Logger
import play.api.i18n._

import anorm._
import anorm.SqlParser._

import helpers._

case class SurveyType(id: Pk[Long], name: String, active: Long)

object SurveyType {
	
	val simple = {
	    get[Pk[Long]]("survey_types.id") ~
	    get[String]("survey_types.name") ~
	    get[Long]("survey_types.active") map {
	    	case id~name~active => SurveyType(id, name, active)
	    }
    }

    def findById(id: Long): Option[SurveyType] = {
	    DB.withConnection { implicit connection =>
	      SQL("select * from survey_types where id = {id}").on(
	        'id -> id
	      ).as(SurveyType.simple.singleOpt)
	    }
  	}

}

case class SurveyStatus(id: Pk[Long], name: String, active: Long)

object SurveyStatus {
	
	val simple = {
	    get[Pk[Long]]("survey_statuses.id") ~
	    get[String]("survey_statuses.name") ~
	    get[Long]("survey_statuses.active") map {
	    	case id~name~active => SurveyStatus(id, name, active)
	    }
    }

    def findById(id: Long): Option[SurveyStatus] = {
	    DB.withConnection { implicit connection =>
	      SQL("select * from survey_statuses where id = {id}").on(
	        'id -> id
	      ).as(SurveyStatus.simple.singleOpt)
	    }
  	}

}

case class SurveyQuestion(
                  id: Pk[Long]                        = NotAssigned, 
                  surveyId: Long                      = 1L, 
                  survey: String                      = "Mike Jones", 
                  question: String                    = "This is a question", 
                  sequenceOrder: Long                 = 1L,
                  createdAt: Option[java.util.Date]   = None,
                  createdBy: Option[Long]             = None,
                  createdEmail: Option[String]        = None,
                  modifiedAt: Option[java.util.Date]  = None,
                  modifiedBy: Option[Long]            = None,
                  modifiedEmail: Option[String]       = None
                  )

object SurveyQuestion {
  
  val simple = {
    get[Pk[Long]]("survey_questions.id") ~
    get[Long]("survey_questions.survey_id") ~
    get[String]("survey_questions.survey") ~
    get[String]("survey_questions.question") ~
    get[Long]("survey_questions.sequence_order") ~
    get[Option[java.util.Date]]("survey_questions.created_at") ~
    get[Option[Long]]("survey_questions.created_by") ~
    get[Option[String]]("survey_questions.created_email") ~
    get[Option[java.util.Date]]("survey_questions.modified_at") ~
    get[Option[Long]]("survey_questions.modified_by") ~
    get[Option[String]]("survey_questions.modified_email") map {
      case id~surveyId~survey~question~sequenceOrder~createdAt~createdBy~createdEmail~modifiedAt~
      		modifiedBy~modifiedEmail => 
            SurveyQuestion(id, surveyId, survey, question, sequenceOrder, createdAt, createdBy, 
            				createdEmail, modifiedAt, modifiedBy, modifiedEmail)
    }
  }

  def insert(surveyQuestion: SurveyQuestion): Long = {
    DB.withConnection { implicit connection =>
      SQL(
        """
        insert into survey_questions (survey_id, survey, question, sequence_order, created_at,
          created_by, created_email, modified_at, modified_by, modified_email) 
        values 
        ({surveyId}, {survey}, {question}, {sequenceOrder}, {createdAt}, {createdBy}, {createdEmail},
          {modifiedAt}, {modifiedBy}, {modifiedEmail})
        """
        ).on(
        'surveyId -> surveyQuestion.surveyId,
        'survey -> surveyQuestion.survey,
        'question -> surveyQuestion.question,
        'sequenceOrder -> surveyQuestion.sequenceOrder,
        'createdAt -> surveyQuestion.createdAt.getOrElse(None),
        'createdBy -> surveyQuestion.createdBy.getOrElse(None),
        'createdEmail -> surveyQuestion.createdEmail.getOrElse(None),
        'modifiedAt -> surveyQuestion.modifiedAt.getOrElse(None),
        'modifiedBy -> surveyQuestion.modifiedBy.getOrElse(None),
        'modifiedEmail -> surveyQuestion.modifiedEmail.getOrElse(None)
        ).executeInsert()
    } match {
      case Some(id) => id
      case None => -1L
    }
  }

  def update(id: Long, surveyQuestion: SurveyQuestion) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
        update survey_questions set survey_id = {surveyId}, survey = {survey}, question = {question},
        sequence_order = {sequenceOrder}, modified_at = {modifiedAt}, modified_by = {modifiedBy},
        modified_email = {modifiedEmail} 
        where id = {id}
        """
        ).on(
        'id -> id,
        'surveyId -> surveyQuestion.surveyId,
        'survey -> surveyQuestion.survey,
        'question -> surveyQuestion.question,
        'sequenceOrder -> surveyQuestion.sequenceOrder,
        'modifiedAt -> Utils.getFormattedCurrentTime(),
        'modifiedBy -> surveyQuestion.modifiedBy,
        'modifiedEmail -> surveyQuestion.modifiedEmail
        ).executeUpdate()
    }
  }

  def findById(id: Long): Option[SurveyQuestion] = {
    DB.withConnection { implicit connection =>
      SQL("select * from survey_questions where id = {id}").on(
        'id -> id
      ).as(SurveyQuestion.simple.singleOpt)
    }
  }

  def findOptions(id: Long): Seq[SurveyQuestionOption] = {
  	DB.withConnection { implicit connection =>
      SQL("select * from survey_question_options where survey_question_id = {id}").on(
        'id -> id
      ).as(SurveyQuestionOption.simple *)
    }
  }

  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from survey_questions where id = {id}").on('id -> id).executeUpdate()
    }
  }

}

case class SurveyQuestionOption(
                  id: Pk[Long]                          = Id(1L), 
                  surveyQuestionId: Long                = 1L, 
                  surveyQuestion: String                = "BLAH", 
                  answer: String                        = "SOMETHING",
                  selectedNum: Long                     = 0L,
                  sequenceOrder: Long                   = 1L,
                  createdAt: Option[java.util.Date]     = None,
                  createdBy: Option[Long]               = None,
                  createdEmail: Option[String]          = None,
                  modifiedAt: Option[java.util.Date]    = None,
                  modifiedBy: Option[Long]              = None,
                  modifiedEmail: Option[String]         = None
                  )

object SurveyQuestionOption {

	val simple = {
	    get[Pk[Long]]("survey_question_options.id") ~
	    get[Long]("survey_question_options.survey_question_id") ~
	    get[String]("survey_question_options.survey_question") ~
	    get[String]("survey_question_options.answer") ~
	    get[Long]("survey_question_options.selected_num") ~
	    get[Long]("survey_question_options.sequence_order") ~
	    get[Option[java.util.Date]]("survey_question_options.created_at") ~
	    get[Option[Long]]("survey_question_options.created_by") ~
	    get[Option[String]]("survey_question_options.created_email") ~
	    get[Option[java.util.Date]]("survey_question_options.modified_at") ~
	    get[Option[Long]]("survey_question_options.modified_by") ~
	    get[Option[String]]("survey_question_options.modified_email") map {
	      case id~surveyQuestionId~surveyQuestion~answer~selectedNum~sequenceOrder~createdAt~createdBy~createdEmail~modifiedAt~
	      		modifiedBy~modifiedEmail => 
	            SurveyQuestionOption(id, surveyQuestionId, surveyQuestion, answer, selectedNum, sequenceOrder, createdAt, createdBy, 
	            				createdEmail, modifiedAt, modifiedBy, modifiedEmail)
	    }
  }

  def insert(sqo: SurveyQuestionOption): Long = {
    DB.withConnection { implicit connection =>
      SQL(
        """
        insert into survey_question_options (survey_question_id, survey_question, answer, selected_num, 
          sequence_order, created_at, created_by, created_email, modified_at, modified_by, modified_email) 
        values 
        ({surveyQuestionId}, {surveyQuestion}, {answer}, {selectedNum}, {sequenceOrder}, {createdAt}, {createdBy}, {createdEmail},
          {modifiedAt}, {modifiedBy}, {modifiedEmail}) 
        """
        ).on(
        'surveyQuestionId -> sqo.surveyQuestionId,
        'surveyQuestion -> sqo.surveyQuestion,
        'answer -> sqo.answer,
        'selectedNum -> sqo.selectedNum,
        'sequenceOrder -> sqo.sequenceOrder,
        'createdAt -> sqo.createdAt.getOrElse(None),
        'createdBy -> sqo.createdBy.getOrElse(None),
        'createdEmail -> sqo.createdEmail.getOrElse(None),
        'modifiedAt -> sqo.modifiedAt.getOrElse(None),
        'modifiedBy -> sqo.modifiedBy.getOrElse(None),
        'modifiedEmail -> sqo.modifiedEmail.getOrElse(None)
        ).executeInsert()
    } match {
      case Some(id) => id
      case None => -1L
    }
  }

  def update(id: Long, sqo: SurveyQuestionOption) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
        update survey_question_options set survey_question_id = {surveyQuestionId}, survey_question = {surveyQuestion}, 
          answer = {answer}, selected_num = {selectedNum}, sequence_order = {sequenceOrder}, modified_at = {modifiedAt}, 
          modified_by = {modifiedBy}, modified_email = {modifiedEmail} 
        where id = {id}
        """
        ).on(
        'id -> id,
        'surveyQuestionId -> sqo.surveyQuestionId,
        'surveyQuestion -> sqo.surveyQuestion,
        'answer -> sqo.answer,
        'selectedNum -> sqo.selectedNum,
        'sequenceOrder -> sqo.sequenceOrder,
        'modifiedAt -> Utils.getFormattedCurrentTime(),
        'modifiedBy -> sqo.modifiedBy,
        'modifiedEmail -> sqo.modifiedEmail
        ).executeUpdate()
    }
  }

  def findById(id: Long): Option[SurveyQuestionOption] = {
    DB.withConnection { implicit connection =>
      SQL("select * from survey_question_options where id = {id}").on(
        'id -> id
      ).as(SurveyQuestionOption.simple.singleOpt)
    }
  }

  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from survey_question_options where id = {id}").on('id -> id).executeUpdate()
    }
  }

}


case class Survey(
                  id: Pk[Long]                          = NotAssigned, 
                  userId: Long                          = -1L, 
                  email: String                         = "", 
                  name: String                          = "", 
                  surveyTypeId: Long                    = 1L,
                  surveyType: String                    = Messages("general.Voice"),
                  surveyStatusId: Long                  = 1L,
                  surveyStatus: String                  = Messages("general.Pending"),
                  greeting: String                      = "",
                  createdAt: Option[java.util.Date]     = None,
                  createdBy: Option[Long]               = None,
                  createdEmail: Option[String]          = None,
                  modifiedAt: Option[java.util.Date]    = None,
                  modifiedBy: Option[Long]              = None,
                  modifiedEmail: Option[String]         = None,
                  planId: Option[Long]                  = None,
                  plan: Option[String]                  = None,
                  numContacts: Option[Long]             = None,
                  numContactsAllowed: Option[Long]      = None,
                  contactListId: Option[Long]           = None,
                  contactList: Option[String]           = None
                  )

object Survey {

	val simple = {
	    get[Pk[Long]]("surveys.id") ~
	    get[Long]("surveys.user_id") ~
	    get[String]("surveys.email") ~
	    get[String]("surveys.name") ~
	    get[Long]("surveys.survey_type_id") ~
	    get[String]("surveys.survey_type") ~
	    get[Long]("surveys.survey_status_id") ~
	    get[String]("surveys.survey_status") ~
	    get[String]("surveys.greeting") ~
	    get[Option[java.util.Date]]("surveys.created_at") ~
	    get[Option[Long]]("surveys.created_by") ~
	    get[Option[String]]("surveys.created_email") ~
	    get[Option[java.util.Date]]("surveys.modified_at") ~
	    get[Option[Long]]("surveys.modified_by") ~
	    get[Option[String]]("surveys.modified_email") ~ 
      get[Option[Long]]("surveys.plan_id") ~
      get[Option[String]]("surveys.plan") ~ 
      get[Option[Long]]("surveys.num_contacts") ~
      get[Option[Long]]("surveys.num_contacts_allowed") ~
      get[Option[Long]]("surveys.contact_list_id") ~
      get[Option[String]]("surveys.contact_list") map {
	      case id~userId~email~name~surveyTypeId~surveyType~surveyStatusId~surveyStatus~greeting~
	      		createdAt~createdBy~createdEmail~modifiedAt~modifiedBy~modifiedEmail~planId~plan~numContacts~
            numContactsAllowed~contactListId~contactList => 
	            Survey(id, userId, email, name, surveyTypeId, surveyType, surveyStatusId, surveyStatus, greeting, 
	            		createdAt, createdBy, createdEmail, modifiedAt, modifiedBy, modifiedEmail, planId, plan,
                  numContacts, numContactsAllowed, contactListId, contactList)
	    }
  }

  def findById(id: Long): Option[Survey] = {
    DB.withConnection { implicit connection =>
      SQL("select * from surveys where id = {id}").on(
        'id -> id
      ).as(Survey.simple.singleOpt)
    }
  }

  def findInvolving(userId: Long, page: Int = 0, pageSize: Int = 20, orderBy: String = "created_at", 
  						order: String = "ASC", filter: String = "%"): 	Page[Survey] = {
  	
	val offset = pageSize * page

  	DB.withConnection { implicit connection =>

		val surveys = SQL(
			"""
			select * from surveys s where s.user_id = {id} and s.name like {filter}
			order by {orderBy} {order} limit {pageSize} offset {offset}
			"""
			).on(
			  'id -> userId,
			  'filter -> filter,
			  'pageSize -> pageSize, 
        	  'offset -> offset,
        	  'orderBy -> orderBy,
        	  'order -> order
      		).as(Survey.simple *)

		val totalRows = SQL(
			"""
			select count(id) from surveys s where s.user_id = {id} and s.name like {filter}
			"""
			).on(
			  'id -> userId,
			  'filter -> filter
      		).as(scalar[Long].single)

      	Page(surveys, page, offset, totalRows)
    }
  }

  def findAll: Seq[Survey] = {
    DB.withConnection { implicit connection =>
      SQL("select * from surveys").as(Survey.simple *)
    }
  }

  def findQuestions(id: Long): Seq[SurveyQuestion] = {
  	DB.withConnection { implicit connection =>
      SQL("select * from survey_questions where survey_id = {id} order by sequence_order").on(
        'id -> id
      ).as(SurveyQuestion.simple *)
    }
  }

  def delete(id: Long) = {
  	DB.withConnection { implicit connection =>
      SQL("delete from surveys where id = {id}").on('id -> id).executeUpdate()
    }
  }

  def update(id: Long, survey: Survey) = {
  	DB.withConnection { implicit connection =>
      SQL(
        """
        update surveys set name = {name}, greeting = {greeting}, modified_at = {modifiedAt},
        survey_status_id = {surveyStatusId}, survey_status = {surveyStatus}, survey_type_id = {surveyTypeId},
        survey_type = {surveyType} 
        where id = {id}
        """
        ).on(
      	'id -> id,
      	'name -> survey.name,
        'greeting -> survey.greeting,
        'surveyTypeId -> survey.surveyTypeId,
        'surveyType -> survey.surveyType,
        'surveyStatusId -> survey.surveyStatusId,
        'surveyStatus -> survey.surveyStatus,
        'modifiedAt -> Utils.getFormattedCurrentTime()
      	).executeUpdate()
    }
  }

  def insert(survey: Survey): Long = {
    DB.withConnection { implicit connection =>
      val time = Utils.getFormattedCurrentTime()
      SQL(
        """
        insert into surveys (user_id, email, name, greeting, modified_at, created_at,
          survey_status_id, survey_status, survey_type_id, survey_type) 
        values 
        ({userId}, {email}, {name}, {greeting}, {modifiedAt}, {createdAt}, {surveyStatusId},
          {surveyStatus}, {surveyTypeId}, {surveyType}) 
        """
        ).on(
        'userId -> survey.userId,
        'email -> survey.email,
        'name -> survey.name,
        'greeting -> survey.greeting,
        'surveyTypeId -> survey.surveyTypeId,
        'surveyType -> survey.surveyType,
        'surveyStatusId -> survey.surveyStatusId,
        'surveyStatus -> survey.surveyStatus,
        'modifiedAt -> time,
        'createdAt -> time
        ).executeInsert()
    } match {
      case Some(id) => id
      case None => -1L
    }
  }

}