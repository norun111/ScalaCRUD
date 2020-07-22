package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scalikejdbc._
import models._
import java.util.UUID
import play.api.libs.json.Json

object PostJsonController {

  //Post情報を受け取る為のケースクラス
  case class PostForm(user_id: String, text: String)

  // PostをJSONに変換するためのWritesを定義
  implicit val postFormWrites = (
    (__ \ "user_id").write[String] and
      (__ \ "text").write[String]
  )(unlift(PostForm.unapply))

  // JSONをPostFormに変換するためのReadsを定義
  implicit val postFormReads = (
    (__ \ "user_id").read[String] and
      (__ \ "text").read[String]
  )(PostForm)

}

class PostJsonController @Inject()(components: ControllerComponents)
    extends AbstractController(components) {

  // コンパニオンオブジェクトに定義したReads、Writesを参照するためにimport文を追加
  import PostJsonController._

  //index API
  def index = Action { implicit request =>
    val posts = DB readOnly { implicit session =>
      sql"""
           select id, user_id, text, comment_count, posted_at from post
         """
        .map { rs =>
          (rs.string("id"),
           rs.string("user_id"),
           rs.string("text"),
           rs.int("comment_count"),
           rs.dateTime("posted_at"),
          )
        }
        .list()
        .apply()
    }
    // Postの一覧をJSONで返す
    Ok(Json.obj("posts" -> posts))
  //posts:[["b26d438a-13bd-497e-87f8-00785343f9f7",0,"hello scala",0,"2020-07-18 16:16:06.691"],
  //後々：posts:[{}]この形式に変換したい
  }

  //create API
  def create = Action(parse.json) { implicit request =>
    request.body
      .validate[PostForm]
      .map { form =>
        // OKの場合はユーザを登録
        DB.localTx { implicit session =>
          //Some(User(11111111-1111-1111-1111-111111111111,alice))

          User.findUser(form.user_id) match {
            case Some(user) =>
              if (form.text.length == 0) {
                //文字列長が0の状態
                BadRequest(
                  (Json.toJson(Response(Meta(400, "Cannot be registered with null text")))))
              } else if (form.text.length >= 101) {
                //文字列長が101の状態
                BadRequest((Json.toJson(
                  Response(Meta(400, "Cannot be registered with more than 101 characters")))))
              } else {
                val uuid = UUID.randomUUID
                Post.create(uuid.toString, form.user_id, form.text)
                Ok(Json.obj("result" -> "OK"))
              }

            case None =>
              //存在しないidの状態
              BadRequest((Json.toJson(Response(Meta(400, s"user_id : ${form.user_id} not found")))))
          }
        }
      }
      .recoverTotal { e =>
        // NGの場合はバリデーションエラーを返す
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }
}
