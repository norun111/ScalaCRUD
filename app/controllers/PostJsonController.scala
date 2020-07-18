package controllers

import java.util.Date

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scalikejdbc._
import models._

object PostJsonController {

  //Post情報を受け取る為のケースクラス
  case class PostForm(text: String, comment_count: Int)

  // PostをJSONに変換するためのWritesを定義
  implicit val postsWrites = (
    (__ \ "text").write[String] and
      (__ \ "comment_count").write[Int]
  )(unlift(PostForm.unapply))

  // JSONをPostFormに変換するためのReadsを定義
  implicit val userFormReads = (
    (__ \ "text").read[String] and
      (__ \ "comment_count").read[Int]
  )(PostForm)
}

class PostJsonController @Inject()(components: ControllerComponents)
    extends AbstractController(components) {

  import PostJsonController._

  def create = Action(parse.json) { implicit request =>
    request.body
      .validate[PostForm]
      .map { form =>
        // OKの場合はユーザを登録
        DB.localTx { implicit session =>
          Post.create(form.text, form.comment_count)
          Ok(Json.obj("result" -> "success"))
        }
      }
      .recoverTotal { e =>
        // NGの場合はバリデーションエラーを返す
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }

}
