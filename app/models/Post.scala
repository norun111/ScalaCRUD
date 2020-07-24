package models

import scalikejdbc._
import java.util.Date
import java.util.UUID

import controllers.PostJsonController.PostIndex
import scalikejdbc.config._

case class Post(id: String = UUID.randomUUID.toString,
                text: String,
                user_id: String,
                comment_count: Int,
                posted_at: Date,
                comments: Seq[Comment] = Nil)

//SQLInterpolation
object Post extends SQLSyntaxSupport[Post] {

  override val tableName = "post"
//  def apply(p: SyntaxProvider[Post])(rs: WrappedResultSet): Post = apply(p.resultName)(rs)
  def apply(p: ResultName[Post])(rs: WrappedResultSet) = new Post(
    rs.string("id"),
    rs.string("text"),
    rs.string("user_id"),
    rs.int("comment_count"),
    rs.date("posted_count")
  )

  val p = Post.syntax("p")

  import Comment.c

  DBs.setupAll()

  def findPost(post_id: String = UUID.randomUUID.toString): Option[Post] =
    DB readOnly { implicit session =>
      sql"""
         SELECT id, text, user_id, comment_count, posted_at
         FROM post
         WHERE id = ${post_id}
      """
        .map { rs =>
          Post(
            id = rs.string("id"),
            text = rs.string("text"),
            user_id = rs.string("user_id"),
            comment_count = rs.int("comment_count"),
            posted_at = rs.timestamp("posted_at")
          )
        }
        .single()
        .apply()
    }

//  def findAllPost: Seq[Post] =
//    DB readOnly { implicit session =>
//      sql"""
//        select
//           *
//        from
//          post
//      """
////        .map { rs =>
////          PostIndex(
////            id = rs.string("id"),
////            text = rs.string("text"),
////            user_id = rs.string("user_id"),
////            comment_count = rs.int("comment_count"),
////            posted_at = rs.timestamp("posted_at")
////          )
////        }
////        .list()
////        .apply()
//    }

  def create(id: String = UUID.randomUUID.toString, user_id: String, text: String): Unit =
    DB localTx { implicit session =>
      sql"""INSERT INTO post (id, user_id, text, comment_count) VALUES (${id},${user_id},${text},0)"""
        .update()
        .apply()
    }
}
