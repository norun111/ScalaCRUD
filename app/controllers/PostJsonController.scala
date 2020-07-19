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
  case class PostForm(id: String,
                      user_id: Long,
                      text: String,
                      comment_count: Int,
                      posted_at: String)

  // PostをJSONに変換するためのWritesを定義
  implicit val postsWrites = (
    (__ \ "id").write[String] and
      (__ \ "user_id").write[Long] and
      (__ \ "text").write[String] and
      (__ \ "comment_count").write[Int] and
      (__ \ "posted_at").write[String]
  )(unlift(PostForm.unapply))

  // JSONをPostFormに変換するためのReadsを定義
  implicit val postsFormReads = (
    (__ \ "id").read[String] and
      (__ \ "user_id").read[Long] and
      (__ \ "text").read[String] and
      (__ \ "comment_count").read[Int] and
      (__ \ "posted_at").read[String]
  )(PostForm)

}

class PostJsonController @Inject()(components: ControllerComponents)
    extends AbstractController(components) {

  // コンパニオンオブジェクトに定義したReads、Writesを参照するためにimport文を追加
  import PostJsonController._

  def index = Action { implicit request =>
    val posts = DB readOnly { implicit session =>
      sql"""
           select id, user_id, text, comment_count, posted_at from post
         """
        .map { rs =>
          (rs.string("id"),
           rs.long("user_id"),
           rs.string("text"),
           rs.int("comment_count"),
           rs.string("posted_at"),
          )
        }
        .list()
        .apply()
    }
    // Postの一覧をJSONで返す
    Ok(Json.obj("posts" -> posts))
  //posts:[["b26d438a-13bd-497e-87f8-00785343f9f7",0,"hello scala",0,"2020-07-18 16:16:06.691"],
  //posts:[{}]この形式に変換したい
  }

  def create = Action(parse.json) { implicit request =>
    request.body
      .validate[PostForm]
      .map { form =>
        // OKの場合はユーザを登録
        DB.localTx { implicit session =>
          //uuidの保存
          val uuid = UUID.randomUUID
          Post.create(uuid.toString, form.user_id, form.text,form.comment_count, form.posted_at)
          Ok(Json.obj("post" -> form))
        }
      }
      .recoverTotal { e =>
        // NGの場合はバリデーションエラーを返す
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }
}
