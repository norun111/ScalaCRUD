package controllers

import java.util._
import play.api.libs.functional.syntax._
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

  implicit val commentWrites: Writes[CommentIndex] = (
    (__ \ "id").write[String] and
      (__ \ "user_id").write[String] and
      (__ \ "text").write[String] and
      (__ \ "parent_post_id").write[String] and
      (__ \ "comment_count").write[Int] and
      (__ \ "posted_at").write[Date]
  )(unlift(CommentIndex.unapply))

  // comment formatter no used
//  case class CommentFormatter(format: String, comments: Seq[CommentIndex])
//
//  implicit val commentFormatWrites: Writes[CommentFormatter] = (
//    (__ \ "format").write[String] and
//      (__ \ "comments").write[Seq[CommentIndex]]
//  )(unlift(CommentFormatter.unapply))

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
                      (Json.toJson(Response(Meta(400, "Cannot be registered with null text")))))
                  } else if (form.text.length >= 101) {
                    //文字列長が101の状態
                    BadRequest((Json.toJson(
                      Response(Meta(400, "Cannot be registered with more than 101 characters")))))
                  } else {
                    Comment.create(uuid.toString, form.user_id, form.text, post_id)
                    Comment.addCommentCount(post_id)
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
                          (Json.toJson(Response(Meta(400, "Cannot be registered with null text")))))
                      } else if (form.text.length >= 101) {
                        //文字列長が101の状態
                        BadRequest((Json.toJson(Response(
                          Meta(400, "Cannot be registered with more than 101 characters")))))
                      } else {
                        Comment.create(uuid.toString, form.user_id, form.text, post_id)
                        Comment.addCommentCountOnComment(post_id)
                        Ok(Json.obj("result" -> "OK"))
                      }

                    case None =>
                      BadRequest((Json.toJson(Response(Meta(400, s"${form.user_id} not found")))))
                  }

                case None =>
                  BadRequest((Json.toJson(Response(Meta(
                    400,
                    s"post_id : ${form.user_id} does not exist in both comment and post tables")))))
              }
          }
        }
      }
      .recoverTotal { e =>
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }

}
