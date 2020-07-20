package controllers

import java.util.{ Date, UUID }
import play.api.libs.functional.syntax._

import controllers.PostJsonController.PostForm
import javax.inject.Inject
import models.Comment
import play.api.libs.json._
import play.api.mvc.{ AbstractController, ControllerComponents }
import scalikejdbc._

object CommentJsonController {

  case class CommentForm(
      user_id: String,
      text: String
  )

  // PostをJSONに変換するためのWritesを定義
  implicit val commentFormWrites = (
    (__ \ "user_id").write[String] and
      (__ \ "text").write[String]
  )(unlift(CommentForm.unapply))

  // JSONをPostFormに変換するためのReadsを定義
  implicit val commentFormReads = (
    (__ \ "user_id").read[String] and
      (__ \ "text").read[String]
  )(CommentForm)
}

class CommentJsonController @Inject()(components: ControllerComponents)
    extends AbstractController(components) {

  import CommentJsonController._

  def index(post_id: Long) = TODO

  //create API
  def create(post_id: String) = Action(parse.json) { implicit request =>
    println(post_id.getClass)
    request.body
      .validate[CommentForm]
      .map { form =>
        // OKの場合はユーザを登録
        DB.localTx { implicit session =>
          val uuid = UUID.randomUUID
          Comment.create(uuid.toString, form.user_id, form.text, post_id)
          Ok(Json.obj("result" -> "OK"))
        }
      }
      .recoverTotal { e =>
        // NGの場合はバリデーションエラーを返す
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }
}
