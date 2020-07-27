package controllers

import models.{Comment, Post, nestComment}
import play.api.libs.json._

object FormatterController {
  implicit val nestCommentFormat = Json.format[nestComment]
  implicit val commentFormat = Json.format[Comment]
  implicit val postFormat = Json.format[Post]
}
