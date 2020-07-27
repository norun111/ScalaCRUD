package controllers

import models.{ nestComment, Comment, Post }
import play.api.libs.json._

object FormatterController {
  implicit val nestCommentFormat = Json.format[nestComment]
  implicit val commentFormat = Json.format[Comment]
  implicit val postFormat = Json.format[Post]
}
