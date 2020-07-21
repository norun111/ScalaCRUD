package controllers

import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax

case class Meta(status: Int, errorMessage: String)

object Meta {

  implicit val metaWrites = (
    (__ \ "status").write[Int] and
      (__ \ "errorMessage").write[String]
  )(unlift(Meta.unapply))
}

case class Response(meta: Meta, data: Option[JsValue] = None)

object Response {

  implicit val responseWrites = (
    (__ \ "meta").write[Meta] and
      (__ \ "data").write[Option[JsValue]]
  )(unlift(Response.unapply))
}
