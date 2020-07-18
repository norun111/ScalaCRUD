package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scalikejdbc._
import models._
import java.util.UUID

object PostJsonController {

  //Post情報を受け取る為のケースクラス
  case class PostForm(id: String, text: String, comment_count: Int)

  // PostをJSONに変換するためのWritesを定義
  implicit val postsWrites = (
    (__ \ "id").write[String] and
      (__ \ "text").write[String] and
      (__ \ "comment_count").write[Int]
  )(unlift(PostForm.unapply))

  // JSONをPostFormに変換するためのReadsを定義
  implicit val userFormReads = (
    (__ \ "id").read[String] and
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
          val uuid = UUID.randomUUID
          Post.create(uuid.toString, form.text, form.comment_count)
          Ok(Json.obj("post" -> form))
        }
      }
      .recoverTotal { e =>
        // NGの場合はバリデーションエラーを返す
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }

}
