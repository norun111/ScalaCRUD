//Mapping Seq to JSON
package models

import play.api.libs.json._

object Formatters {
  implicit val commentFormat = Json.format[Comment]
  implicit val postFormat = Json.format[Post]
}
