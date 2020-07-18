//package controllers
//
//import java.util.Date
//
//import javax.inject._
//import models.{ Post, _ }
//import play.api.libs.functional.syntax._
//import play.api.libs._
//import play.api.mvc._
//import play.api.data._
//import play.api.data.Forms._
//import scalikejdbc._
//import models._
//import scalikejdbc.config._
//import PostJsonController._
//
//case class Post(id: Long,
//                text: String,
//                userId: Option[Long] = None,
//                comment_count: Int,
//                posted_at: Date,
//                user: Option[User] = None)
//
//object Post {
//
//  DBs.setupAll()
//
//  def findAll: Seq[Post] = DB readOnly { implicit session =>
//    sql"SELECT id, text, commented_count, posted_at FROM POST"
//      .map { rs =>
//        Post(
//          rs.long("id"),
//          rs.string("text"),
//          rs.longOpt("userId"),
//          rs.int("comment_count"),
//          rs.date("posted_at")
//        )
//      }
//      .list()
//      .apply()
//  }
//
//  def create(post: Post): Unit = DB localTx { implicit session =>
//    sql"INSERT INTO post (text) VALUES (${post.text})".update().apply()
//  }
//}
//
//@Singleton
//class PostController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
//
//  def index = TODO
////    Action { implicit request =>
////    DB.readOnly { implicit session =>
////      val posts: List[Post] = withSQL {
////        select.from(Post as p).leftJoin(User as u).on(p.userId, u.id).orderBy(p.id.asc)
////      }.map(Post(p, u)).list.apply()
////
////      val pos: Seq[(Post, User)] = sql"""
////        |SELECT ${p.result.*}, ${u.result.*}
////        |FROM ${Post.as(p)} INNER JOIN ${User.as(u)}
////        |ON ${p.userId} = ${u.id}
////      """.stripMargin
////        .map { rs =>
////          (Post(p)(rs), User(u)(rs))
////        }
////        .list
////        .apply()
////
////      Ok(Json.obj("posts" -> pos))
////    }
////  }
//
//  // フォームの値を格納するケースクラス
//  case class PostForm(
//      text: String, //                      userId: Option[Long] = None,
////                      commentCount: Int)
//  )
////  // formから送信されたデータ ⇔ ケースクラスの変換を行う
//  private val postForm = Form(
//    mapping(
////      "id" -> optional(longNumber),
//      "text" -> nonEmptyText(maxLength = 20),
////      "userId" -> optional(longNumber),
////      "commentCount" -> number,
//    )(PostForm.apply)(PostForm.unapply))
//
//  def create = Action { implicit req =>
//    postForm.bindFromRequest.fold(
//      formWithErrors => BadRequest("invalid parameters"),
//      form => {
//        val post = Post.create(text = form.text)
//        Ok(Json.obj("posts" -> post))
//        NoContent
//      }
//    )
//  }
//}
