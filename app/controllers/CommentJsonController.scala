package controllers

import java.util.{ Date, UUID }
import play.api.libs.functional.syntax._

import controllers.PostJsonController.PostForm
import javax.inject.Inject
import models._
import play.api.libs.json._
import play.api.mvc.{ AbstractController, ControllerComponents }
import scalikejdbc._

object CommentJsonController {

  // Index API Json
  case class CommentIndex(
      id: String = UUID.randomUUID.toString,
      user_id: String,
      text: String,
      parent_post_id: String,
      comment_count: Int,
      posted_at: Date
  )

  implicit val commentIndexWrites = (
    (__ \ "id").write[String] and
      (__ \ "user_id").write[String] and
      (__ \ "text").write[String] and
      (__ \ "parent_post_id").write[String] and
      (__ \ "comment_count").write[Int] and
      (__ \ "posted_at").write[Date]
  )(unlift(CommentIndex.unapply))

  implicit val commentIndexReads = (
    (__ \ "id").read[String] and
      (__ \ "user_id").read[String] and
      (__ \ "text").read[String] and
      (__ \ "parent_post_id").read[String] and
      (__ \ "comment_count").read[Int] and
      (__ \ "posted_at").read[Date]
  )(CommentIndex)

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

  //index API
  def index(post_id: String) = Action { implicit request =>
    DB readOnly { implicit session =>
      val comments = sql"""
           select id, user_id, text, parent_post_id, comment_count, posted_at
           from comment
           where parent_post_id = ${post_id}
         """
        .map { rs =>
          (rs.string("id"),
           rs.string("user_id"),
           rs.string("text"),
           rs.string("parent_post_id"),
           rs.int("comment_count"),
           rs.dateTime("posted_at"),
          )
        }
        .list()
        .apply()

      Ok(Json.obj("comments" -> Json.toJson(comments)))
    }

  }

  //create API
  def create(post_id: String) = Action(parse.json) { implicit request =>
    request.body
      .validate[CommentForm]
      .map { form =>
        // OKの場合はユーザを登録
        DB.localTx { implicit session =>
          val referencePost = Comment.findPost(post_id)
          val referencePostId = referencePost.get.id

          val referenceUser = Post.findUser(form.user_id)

          if (referenceUser.isDefined) {
            if (post_id == referencePostId) {
              val uuid = UUID.randomUUID
              Comment.create(uuid.toString, form.user_id, form.text, post_id)
              Comment.addComment(post_id)
              Ok(Json.obj("result" -> "OK"))
            } else {

              //エラー処理
              Ok(Json.obj("result" -> "FAIL"))
            }
          } else {
            //エラー処理
            Ok(Json.obj("result" -> "FAIL"))
          }
        }
      }
      .recoverTotal { e =>
        // NGの場合はバリデーションエラーを返す
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }
}
