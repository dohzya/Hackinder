package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api._
import play.api.mvc._
import play.api.Play.current

import models.Hacker

case class CtxRequest[A](request: Request[A], ctx: Ctx) extends WrappedRequest(request)

trait Context {
  this: Controller =>

  implicit def ctx(implicit req: CtxRequest[_]) = req.ctx
  implicit def me(implicit ctx: Ctx) = ctx.hacker

  object WithContext extends ActionBuilder[CtxRequest] {
    def invokeBlock[A](request: Request[A], block: (CtxRequest[A]) => Future[SimpleResult]) = {
      OAuth2.authenticatedAction[A](request, req => block(CtxRequest(req.request, Ctx(req.hacker))))
    }
  }

}

case class Ctx(
  hacker: Hacker
)
