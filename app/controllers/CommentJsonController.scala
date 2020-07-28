package controllers

import java.time.format.DateTimeFormatter
import java.util._

import javax.inject.Inject
import models._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import scalikejdbc._

object CommentJsonController {

  //時間のフォーマットが上手くいかなかった為DSLを使わず記述
  implicit val commentWrites = new Writes[Comment] {
    def writes(comment: Comment): JsValue = {
      Json.obj(
        "id" -> comment.id,
        "user_id" -> comment.user_id,
        "text" -> comment.text,
        "parent_post_id" -> comment.parent_post_id,
        "comment_count" -> comment.comment_count,
        "posted_at" -> comment.posted_at
      )
    }
  }

  //Formを送信する際のケースクラスを定義
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

  //任意のpost_idに紐づくComment一覧を取得
  def index(post_id: String) = Action { implicit request =>
    val comments = Comment.findAllComment(post_id)
    Ok(Json.obj("comments" -> Json.toJson(comments)))
  }

  //任意のpost_idに紐づくCommentを新規に作成
  def create(post_id: String) = Action(parse.json) { implicit request =>
    request.body
      .validate[CommentForm]
      .map { form =>
        DB.localTx { implicit session =>
          val uuid = UUID.randomUUID

          // post_idに紐づくPostが存在するかどうかを確認
          Post.findPost(post_id) match {
            case Some(post) =>
              //Formに送信されたuser_idがuserテーブルに存在するかどうか確認
              User.findUser(form.user_id) match {
                //Formに送信されたuser_idがuserテーブルに存在した場合
                case Some(user) =>
                  if (form.text.length == 0) {
                    //文字列長が0の状態
                    BadRequest(
                      (Json.toJson(Response(Meta(400, "Can't be registered with null text")))))
                  } else if (form.text.length >= 101) {
                    //文字列長が100良い長い状態
                    BadRequest((Json.toJson(
                      Response(Meta(400, "Can't be registered with more than 100 characters")))))
                  } else {
                    Comment.create(uuid.toString, form.user_id, form.text, post_id)
                    //post_idとPostのidが一致するレコードのcomment_countカラムの値を+1する
                    Post.addCommentCount(post_id)
                    Ok(Json.obj("result" -> "OK"))
                  }
                //Formに送信されたuser_idがuserテーブルに存在しなかった場合
                case None =>
                  BadRequest(
                    (Json.toJson(Response(Meta(400, s"user_id : ${form.user_id} not found")))))
              }

            case None =>
              //post_idに紐づくPostが存在しなかった場合、そのpost_idに紐づくCommentがあるかどうか確認
              Comment.findComment(post_id) match {
                case Some(comment) =>
                  //Formに送信されたuser_idがuserテーブルに存在するかどうか確認
                  User.findUser(form.user_id) match {
                    //Formに送信されたuser_idがuserテーブルに存在した場合
                    case Some(user) =>
                      if (form.text.length == 0) {
                        //文字列長が0の状態
                        BadRequest(
                          (Json.toJson(Response(Meta(400, "Can't be registered with null text")))))
                      } else if (form.text.length >= 101) {
                        //文字列長が100より長い状態
                        BadRequest((Json.toJson(Response(
                          Meta(400, "Can't be registered with more than 100 characters")))))
                      } else {
                        Comment.create(uuid.toString, form.user_id, form.text, post_id)
                        //post_idとCommentのidが一致するレコードのcomment_countカラムの値を+1する
                        Comment.addCommentCount(post_id)
                        Ok(Json.obj("result" -> "OK"))
                      }
                    //Formに送信されたuser_idがuserテーブルに存在しなかった場合
                    case None =>
                      BadRequest((Json.toJson(Response(Meta(400, s"user_id : ${form.user_id} not found")))))
                  }
                // post_idに紐づくレコードがPostテーブルにもCommentテーブルにも存在しない場合
                case None =>
                  BadRequest((Json.toJson(Response(
                    Meta(400, s"post_id : ${post_id} not found")))))
              }
          }
        }
      }
      .recoverTotal { e =>
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }

}
