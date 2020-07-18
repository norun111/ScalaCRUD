package controllers

import javax.inject._
import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scalikejdbc._
import models._
import scalikejdbc.config._
import JsonController._


@Singleton
class PostController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action { implicit request =>


    DB.readOnly { implicit session =>
      val posts: List[Post] = withSQL {
        select.from(Post as p).leftJoin(User as u).on(p.userId, u.id).orderBy(p.id.asc)
      }.map(Post(p, u)).list.apply()

      val posts: Seq[(Post, User)] = sql"""
        |SELECT ${p.result.*}, ${u.result.*}
        |FROM ${Post.as(p)} INNER JOIN ${User.as(u)}
        |ON ${p.userId} = ${u.id}
      """.stripMargin.map { rs =>
              (Post(p)(rs), User(u)(rs))
            }.list.apply()

      Ok(Json.obj("posts" -> posts))
    }
  }

  // フォームの値を格納するケースクラス
  case class PostForm(id: Option[Long],
                      text: String,
                      userId: Option[Long] = None,
                      commentCount: Int)
  // formから送信されたデータ ⇔ ケースクラスの変換を行う
  private val postForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "text" -> nonEmptyText(maxLength = 20),
      "userId" -> optional(longNumber),
      "commentCount" -> number,
    )(PostForm.apply)(PostForm.unapply))

  def create = TODO
}
