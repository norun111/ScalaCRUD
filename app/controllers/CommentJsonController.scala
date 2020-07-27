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
    val comments = Comment.findAllComment(post_id)
    Ok(Json.obj("comments" -> Json.toJson(comments)))
  }

  //create API
  def create(post_id: String) = Action(parse.json) { implicit request =>
    request.body
      .validate[CommentForm]
      .map { form =>
        DB.localTx { implicit session =>
          val uuid = UUID.randomUUID

          Post.findPost(post_id) match {
            case Some(post) =>
              User.findUser(form.user_id) match {
                case Some(user) =>
                  if (form.text.length == 0) {
                    //文字列長が0の状態
                    BadRequest(
                      (Json.toJson(Response(Meta(400, "Can't be registered with null text")))))
                  } else if (form.text.length >= 101) {
                    //文字列長が101の状態
                    BadRequest((Json.toJson(
                      Response(Meta(400, "Can't be registered with more than 100 characters")))))
                  } else {
                    Comment.create(uuid.toString, form.user_id, form.text, post_id)
                    Post.addCommentCount(post_id)
                    Ok(Json.obj("result" -> "OK"))
                  }

                case None =>
                  BadRequest(
                    (Json.toJson(Response(Meta(400, s"user_id : ${form.user_id} not found")))))
              }

            case None =>
              Comment.findComment(post_id) match {
                case Some(comment) =>
                  User.findUser(form.user_id) match {
                    case Some(user) =>
                      if (form.text.length == 0) {
                        //文字列長が0の状態
                        BadRequest(
                          (Json.toJson(Response(Meta(400, "Can't be registered with null text")))))
                      } else if (form.text.length >= 101) {
                        //文字列長が101の状態
                        BadRequest((Json.toJson(Response(
                          Meta(400, "Can't be registered with more than 100 characters")))))
                      } else {
                        Comment.create(uuid.toString, form.user_id, form.text, post_id)
                        Comment.addCommentCount(post_id)
                        Ok(Json.obj("result" -> "OK"))
                      }

                    case None =>
                      BadRequest((Json.toJson(Response(Meta(400, s"user_id : ${form.user_id} not found")))))
                  }

                case None =>
                  BadRequest((Json.toJson(Response(
                    Meta(400,
                         s"post_id : ${post_id} not found")))))
              }
          }
        }
      }
      .recoverTotal { e =>
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }

}
