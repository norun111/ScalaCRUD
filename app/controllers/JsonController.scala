package controllers

import models.Post
import play.api.libs.json.{JsValue, Json, Writes}

object JsonController {
  // PostをJSONに変換するためのWritesを定義
  //  implicit val postsWrites = (
  //    (__ \ "id"       ).write[Long]   and
  //      (__ \ "user_id"     ).writeNullable[Int] and
  //      (__ \ "text"     ).write[String] and
  //      (__ \ "comment_count").write[Int] and
  //      (__ \ "posted_at").write[String] and
  //    )(unlift(Post.unapply))

  implicit val postWritesFormat = new Writes[Post] {
    def writes(post: Post): JsValue = {
      Json.obj(
        "id" -> post.id,
        "user_id" -> post.userId,
        "text" -> post.text,
        "comment_count" -> post.commentCount,
        "posted_at" -> post.posted_at
      )
    }
  }
}