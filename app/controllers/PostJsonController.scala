package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import akka.http.scaladsl.model._
import scalikejdbc._
import models._
import java.util.UUID
import java.util.Date

object PostJsonController {

  //Post情報を受け取る為のケースクラス
  case class PostForm(id: String,
                      user_id: String,
                      text: String,
                      comment_count: Int,
                      posted_at: Date)

  // PostをJSONに変換するためのWritesを定義
  implicit val postFormWrites = (
    (__ \ "id").write[String] and
      (__ \ "user_id").write[String] and
      (__ \ "text").write[String] and
      (__ \ "comment_count").write[Int] and
      (__ \ "posted_at").write[Date]
  )(unlift(PostForm.unapply))

  // JSONをPostFormに変換するためのReadsを定義
  implicit val postFormReads = (
    (__ \ "id").read[String] and
      (__ \ "user_id").read[String] and
      (__ \ "text").read[String] and
      (__ \ "comment_count").read[Int] and
      (__ \ "posted_at").read[Date]
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
           rs.timestamp("posted_at"),
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

//    validate[PostForm]は一覧表示 PostFormは名前的にcreateにしよう
  //もう一つcreate用のcase classを作成　そこでimplicitを使用

  //create API
  def create = Action(parse.json) { implicit request =>
    request.body
      .validate[PostForm]
      .map { form =>
        // OKの場合はユーザを登録
        DB.localTx { implicit session =>
          val user = Post.findUser(form.user_id)
          //Some(User(11111111-1111-1111-1111-111111111111,alice))

          println(user.get.name) //後で削除：User nameの取得

          if (user.isDefined) {
            //uuidで保存
            val uuid = UUID.randomUUID
            Post.create(uuid.toString, form.user_id, form.text, form.comment_count, form.posted_at)

            Ok(Json.obj("result" -> "OK"))
            Ok(Json.obj("post" -> form))
          } else {
            //後々：エラー処理しないといけない(必須)
            Ok(Json.obj("post" -> form))
          }
        }
      }
      .recoverTotal { e =>
        // NGの場合はバリデーションエラーを返す
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }
}
