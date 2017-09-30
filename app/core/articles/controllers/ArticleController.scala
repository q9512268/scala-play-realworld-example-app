package core.articles.controllers

import core.articles.controllers.mappings.ArticleJsonMappings
import core.articles.models._
import core.articles.services.ArticleService
import core.authentication.api.AuthenticatedActionBuilder
import commons.models._
import commons.repositories.ActionRunner
import core.commons.controllers.RealWorldAbstractController
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class ArticleController(authenticatedAction: AuthenticatedActionBuilder,
                        actionRunner: ActionRunner,
                        articleService: ArticleService,
                        components: ControllerComponents,
                        implicit private val ec: ExecutionContext)
  extends RealWorldAbstractController(components)
    with ArticleJsonMappings {

  def all(pageRequest: PageRequest): Action[AnyContent] = {
    Action.async {
      actionRunner.runInTransaction(articleService.all(pageRequest))
        .map(page => ArticlePage(page.models, page.count))
        .map(Json.toJson(_))
        .map(Ok(_))
    }
  }

  def create: Action[JsValue] = {
    authenticatedAction.async(parse.json) { request =>
      request.body.validate[NewArticleWrapper].fold(
        errors => Future.successful(BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))),
        articleWrapper => actionRunner.runInTransaction(articleService.create(articleWrapper.article))
          .map(ArticleWrapper)
          .map(Json.toJson(_))
          .map(Ok(_))
      )
    }
  }

}