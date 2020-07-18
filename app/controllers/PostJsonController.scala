package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scalikejdbc._
import models._
import java.util.UUID

object PostJsonController {

  //Post情報を受け取る為のケースクラス
  case class PostForm(id: String,
                      user_id: Long,
                      text: String,
                      comment_count: Int,
                      posted_at: String) {
    val jsonId = Array(id, user_id, text, comment_count, posted_at).mkString("-")

    def toJsonString(): String = Json.toJson(this)(postsWritesFormat).toString()

    def fromJsonString(jsonString: String): PostForm = {
      val jv = Json.parse(jsonString)
      Json.fromJson[PostForm](jv)(postsFormReads).get
    }
  }

  // PostをJSONに変換するためのWritesを定義
//  implicit val postsWrites = (
//    (__ \ "id").write[String] and
//      (__ \ "user_id").write[Long] and
//      (__ \ "text").write[String] and
//      (__ \ "comment_count").write[Int] and
//      (__ \ "posted_at").write[String]
//  )(unlift(PostForm.unapply))

  // JSONをPostFormに変換するためのReadsを定義
  implicit val postsFormReads = (
    (__ \ "id").read[String] and
      (__ \ "user_id").read[Long] and
      (__ \ "text").read[String] and
      (__ \ "comment_count").read[Int] and
      (__ \ "posted_at").read[String]
  )(PostForm)

  implicit val postsWritesFormat = new Writes[PostForm] {
    def writes(post: PostForm): JsValue = {
      Json.obj(
        "id" -> post.id,
        "text" -> post.text,
        "user_id" -> post.user_id,
        "comment_count" -> post.comment_count,
        "posted_at" -> post.posted_at
      )
    }
  }

}

class PostJsonController @Inject()(components: ControllerComponents)
    extends AbstractController(components) {

  // コンパニオンオブジェクトに定義したReads、Writesを参照するためにimport文を追加
  import PostJsonController._

  def queryPostList: List[Map[String, Any]] = {
    DB readOnly { implicit session =>
      sql"""
           select id, text from post
         """.map(_.toMap).list.apply()
    }
  }

  /* queryList
  List(Map(ID -> b26d438a-13bd-497e-87f8-00785343f9f7, TEXT -> clob0: 'hello scala'),
  Map(ID -> e5c17765-4112-42f7-acb5-0bf0064982f3, TEXT -> clob1: 'hello scala'),
  Map(ID -> 23d96945-8ec3-48af-8566-36d5d961229c, TEXT -> clob2: 'hello scala'),
  Map(ID -> 00000000-0000-0000-0000-000000000002, TEXT -> clob3: 'hello scala'),
  Map(ID -> 59d309fe-84b6-4b90-aab6-14598d3f9668, TEXT -> clob4: 'hello scala'))
   */

  def index = Action { implicit request =>
    val posts = queryPostList
//    println(posts)
    Json.toJson（posts）
//    posts.foreach { m =>
//      print(m)
//    }
    val pos = DB readOnly { implicit session =>
      sql"select id, text from post"
        .map { rs =>
          rs.string("id")
          rs.string("text")
        }
        .list()
        .apply()
    }
    // Postの一覧をJSONで返す
    Ok(Json.obj("posts" -> pos))
  //{"posts":["hello scala","hello scala","hello scala","hello scala","hello scala"]}
  }

  def create = Action(parse.json) { implicit request =>
    request.body
      .validate[PostForm]
      .map { form =>
        // OKの場合はユーザを登録
        DB.localTx { implicit session =>
          //uuidの保存
          val uuid = UUID.randomUUID

          Post.create(uuid.toString, form.text, form.comment_count)
          Ok(Json.obj("post" -> form))
        }
      }
      .recoverTotal { e =>
        // NGの場合はバリデーションエラーを返す
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
  }
}
