package models

import play.api.db._
import play.api.Play.current
import play.api.Logger

import anorm._
import anorm.SqlParser._

case class Session(
                  id: Pk[Long], 
                  userId: Long, 
                  email: String, 
                  sessionKey: String, 
                  loginTime: java.util.Date,
                  expiryTime: java.util.Date,
                  ip: String
                  )

object Session {

  val simple = {
    get[Pk[Long]]("sessions.id") ~
    get[Long]("sessions.user_id") ~
    get[String]("sessions.email") ~
    get[String]("sessions.session_key") ~
    get[java.util.Date]("sessions.login_time") ~
    get[java.util.Date]("sessions.expiry_time") ~ 
    get[String]("sessions.ip") map {
      case id~userId~email~sessionKey~loginTime~expiryTime~ip => 
        Session(id, userId, email, sessionKey, loginTime, expiryTime, ip)
    }
  }
}   

case class UserType(id: Pk[Long], name: String, active: Long)

object UserType {

  val simple = {
      get[Pk[Long]]("user_types.id") ~
      get[String]("user_types.name") ~
      get[Long]("user_types.active") map {
        case id~name~active => UserType(id, name, active)
      }
    }

    def findById(id: Long): Option[UserType] = {
      DB.withConnection { implicit connection =>
        SQL("select * from user_types where id = {id}").on(
          'id -> id
        ).as(UserType.simple.singleOpt)
      }
    }
}

case class UserStatus(id: Pk[Long], name: String, active: Long)

object UserStatus {
  
  val simple = {
      get[Pk[Long]]("user_statuses.id") ~
      get[String]("user_statuses.name") ~
      get[Long]("user_statuses.active") map {
        case id~name~active => UserStatus(id, name, active)
      }
    }

    def findById(id: Long): Option[UserStatus] = {
      DB.withConnection { implicit connection =>
        SQL("select * from user_statuses where id = {id}").on(
          'id -> id
        ).as(UserStatus.simple.singleOpt)
      }
    }

}

case class User(
                  id: Pk[Long], 
                  fname: String, 
                  lname: String, 
                  fullname: String, 
                  email: String, 
                  password: String,
                  userTypeId: Option[Long], 
                  userType: Option[String], 
                  userStatusId: Option[Long], 
                  userStatus: Option[String],
                  lastLoginTime: Option[java.util.Date],
                  createdAt: Option[java.util.Date],
                  createdBy: Option[Long],
                  createdEmail: Option[String],
                  modifiedAt: Option[java.util.Date],
                  modifiedBy: Option[Long],
                  modifiedEmail: Option[String],
                  planId: Option[Long],
                  plan: Option[String],
                  numSurveysLeft: Option[Long],
                  numBroadcastsLeft: Option[Long],
                  planExpiryDate: Option[java.util.Date]
                )

object User {
  
  // -- Parsers
  
  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("users.id") ~
    get[String]("users.fname") ~
    get[String]("users.lname") ~
    get[String]("users.fullname") ~
    get[String]("users.email") ~
    get[String]("users.password") ~
    get[Option[Long]]("users.user_type_id") ~
    get[Option[String]]("users.user_type") ~
    get[Option[Long]]("users.user_status_id") ~
    get[Option[String]]("users.user_status") ~
    get[Option[java.util.Date]]("users.last_login_time") ~
    get[Option[java.util.Date]]("users.created_at") ~
    get[Option[Long]]("users.created_by") ~
    get[Option[String]]("users.created_email") ~
    get[Option[java.util.Date]]("users.modified_at") ~
    get[Option[Long]]("users.modified_by") ~
    get[Option[String]]("users.modified_email") ~
    get[Option[Long]]("users.plan_id") ~
    get[Option[String]]("users.plan") ~
    get[Option[Long]]("users.num_surveys_left") ~
    get[Option[Long]]("users.num_broadcasts_left") ~
    get[Option[java.util.Date]]("users.plan_expiry_date") map {
      case id~fname~lname~fullname~email~password~userTypeId~userType~userStatusId~userStatus~
            lastLoginTime~createdAt~createdBy~createdEmail~modifiedAt~modifiedBy~modifiedEmail~
            planId~plan~numSurveysLeft~numBroadcastsLeft~planExpiryDate => 
            User(id, fname, lname, fullname, email, password, userTypeId, userType, userStatusId, userStatus,
            lastLoginTime, createdAt, createdBy, createdEmail, modifiedAt, modifiedBy, modifiedEmail,
            planId, plan, numSurveysLeft, numBroadcastsLeft, planExpiryDate)
    }
  }

  val withSession = User.simple ~ (Session.simple ?) map {
    case user~session => (user,session)
  }
  
  // -- Queries
  
  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from users where email = {email}").on(
        'email -> email
      ).as(User.simple.singleOpt)
    }
  }

  def findWithSessionByEmail(email: String): Option[(User, Option[Session])] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select users.*, sessions.id as session_id, sessions.user_id, sessions.email as session_email,
            sessions.session_key,sessions.login_time, sessions.expiry_time, sessions.ip 
            from users left join sessions on users.id = sessions.user_id where users.email = {email}
          """
            ).on(
        'email -> email
      ).as(User.withSession.singleOpt)
    }
  }
  
  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from users").as(User.simple *)
    }
  }
  
  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      val user: Option[User] = SQL(
        """
         select * from users where 
         email = {email} and password = {password}
        """
      ).on(
        'email -> email,
        'password -> password
      ).as(User.simple.singleOpt)
      
      user
    }
  }
 
  /**
   * Create a User.
   */
  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into users (id, fname, lname, fullname, email, password, user_type_id, user_type, user_status_id, user_status,
            last_login_time, created_at, created_by, created_email, modified_at, modified_by, modified_email) values (
            {id}, {fname}, {lname}, {fullname}, {email}, {password}, {user_type_id}, {user_type}, {user_status_id}, {user_status},
            {last_login_time}, {created_at}, {created_by}, {created_email}, {modified_at}, {modified_by}, {modified_email}
          )
        """
      ).on(
        'id -> user.id,
        'fname -> user.fname,
        'lname -> user.lname,
        'fullname -> user.fullname,
        'email -> user.email,
        'password -> user.password,
        'user_type_id -> user.userTypeId,
        'user_type -> user.userType,
        'user_status_id -> user.userStatusId,
        'user_status -> user.userStatus,
        'last_login_time -> user.lastLoginTime,
        'created_at -> user.createdAt,
        'created_by -> user.createdBy,
        'created_email -> user.createdEmail,
        'modified_at -> user.modifiedAt,
        'modified_by -> user.modifiedBy,
        'modified_email -> user.modifiedEmail
      ).executeUpdate()
      
      user
      
    }
  }
  
}