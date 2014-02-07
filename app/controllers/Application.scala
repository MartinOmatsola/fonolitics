package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.libs.Crypto

import anorm._
import models._
import helpers._

object Application extends Controller with Secured {
  
  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying (Messages("app.auth_failure_message"), result => result match {
      case (email, password) => User.authenticate(email, Crypto.sign(password)).isDefined
    })
  )	

  def index = withUser { user => implicit request =>
    Ok(views.html.index(user))
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action { implicit request =>
    val k = request.session.get("session_key").getOrElse("x")
    
    DB.withConnection { implicit connection =>
      SQL("delete from sessions where session_key = {session_key}").on('session_key -> k).executeUpdate()
    }

    Redirect(routes.Application.login).withNewSession
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => {
          
          val sessionKey = Utils.randomAlphanumericString(64)

          User.findByEmail(user._1).foreach { u => 
            
            DB.withConnection { implicit connection =>
              
              SQL("delete from sessions where email = {email}").on('email -> u.email).executeUpdate()

              val loginTime = Utils.getFormattedCurrentTime()  
              
              SQL(
              """
              insert into sessions (user_id, email, session_key, login_time, expiry_time, ip) values 
                ({user_id}, {email}, {session_key}, {login_time}, {expiry_time}, {ip})
              """
              ).on(
                  'user_id -> u.id,
                  'email -> u.email,
                  'session_key -> sessionKey,
                  'login_time -> loginTime,
                  'expiry_time -> Utils.getFormattedExpiryTime(),
                  'ip -> request.remoteAddress //if apache front-end -> request.headers.get("x-forwarded-for")
              ).executeUpdate()

              SQL("""
                update users set last_login_time = {last_login_time} where id = {id}
                """
                ).on(
                  'last_login_time -> loginTime,
                  'id -> u.id
                ).executeUpdate()

            }
        }

        Redirect(routes.Application.index).withSession("email" -> user._1, "session_key" -> sessionKey)
      }
    )
  }
  
}

/**
 * Provide security features
 */
trait Secured {
  
  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("email")

  private def sessionKey(request: RequestHeader) = request.session.get("session_key")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)
  
  // --
  
  /** 
   * Action for authenticated users.
   */
  def withAuth(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }

  def withUser(f: User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    User.findWithSessionByEmail(username).map { 
      case (user, session) => if (session.map(_.sessionKey).get != sessionKey(request).getOrElse("x") ||
                                    request.remoteAddress != session.map(_.ip).get ||
                                    Utils.getCurrentTime().after(session.map(_.expiryTime).get)) 
                                onUnauthorized(request) 
                              else 
                                f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }



}