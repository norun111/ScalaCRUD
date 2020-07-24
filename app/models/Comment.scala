package models

import java.util.{ Date, UUID }

import controllers.CommentJsonController.CommentIndex
import scalikejdbc._
import scalikejdbc.config._
import play.api.libs.json._

case class Comment(
    id: String = UUID.randomUUID.toString,
    user_id: String,
    text: String,
    parent_post_id: String,
    comment_count: Int,
    posted_at: Date
)

object Comment extends SQLSyntaxSupport[Comment] {

  override val tableName = "comment"

//  def apply(c: SyntaxProvider[Comment])(rs: WrappedResultSet): Comment = apply(c.resultName)(rs)
//
  def apply(c: ResultName[Comment])(rs: WrappedResultSet) = new Comment(
    rs.string("id"),
    rs.string("user_id"),
    rs.string("text"),
    rs.string("parent_post_id"),
    rs.int("comment_count"),
    rs.date("posted_count")
  )
//
//  def opt(c: SyntaxProvider[Comment])(rs: WrappedResultSet) =
//    rs.stringOpt(c.resultName.id).map(_ => Comment(c)(rs))

  DBs.setupAll()

  var c = Comment.syntax("c")

  def findAllComment(post_id: String = UUID.randomUUID.toString): Seq[CommentIndex] =
    DB readOnly { implicit session =>
      sql"""
         SELECT *
         FROM comment
         WHERE parent_post_id = ${post_id}
      """
        .map { rs =>
          CommentIndex(
            id = rs.string("id"),
            user_id = rs.string("user_id"),
            text = rs.string("text"),
            parent_post_id = rs.string("parent_post_id"),
            comment_count = rs.int("comment_count"),
            posted_at = rs.timestamp("posted_at")
          )
        }
        .list()
        .apply()
    }

  //コメント先のコメントを検索
  def findComment(comment_id: String = UUID.randomUUID.toString): Option[Comment] =
    DB readOnly { implicit session =>
      sql"""
         SELECT *
         FROM comment
         WHERE id = ${comment_id}
      """
        .map { rs =>
          Comment(
            id = rs.string("id"),
            user_id = rs.string("user_id"),
            text = rs.string("text"),
            parent_post_id = rs.string("parent_post_id"),
            comment_count = rs.int("comment_count"),
            posted_at = rs.timestamp("posted_at")
          )
        }
        .single()
        .apply()
    }

  def create(id: String = UUID.randomUUID.toString,
             user_id: String,
             text: String,
             parent_post_id: String = UUID.randomUUID.toString): Unit =
    DB localTx { implicit session =>
      sql"""INSERT INTO comment
                 (id, user_id, text, parent_post_id, comment_count)
                 VALUES
                 (${id} ,${user_id},${text}, ${parent_post_id} , 0)
                 """.update().apply()
    }

  //親Postのコメント数を+1
  def addCommentCount(post_id: String = UUID.randomUUID.toString) =
    DB autoCommit { implicit session =>
      sql"""UPDATE post SET comment_count = comment_count + 1
    WHERE id = ${post_id}
    """.update().apply()
    }

  //親Commentのコメント数を+1
  def addCommentCountOnComment(comment_id: String = UUID.randomUUID.toString) =
    DB autoCommit { implicit session =>
      sql"""UPDATE comment SET comment_count = comment_count + 1
    WHERE id = ${comment_id}
    """.update().apply()
    }
  /*
  // not used
  //値を0で挿入し、その後にpost.comment_count + 1でupdate
  def setCommentCount(post_id: String = UUID.randomUUID.toString) =
    DB autoCommit { implicit session =>
      sql"""UPDATE comment SET comment.comment_count = post.comment_count + 1,
            FROM comment
            INNER JOIN post
            ON ${post_id} = post.id
    """.update().apply()
    }

 */

}
