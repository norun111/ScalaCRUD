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

object Comment {

  DBs.setupAll()

  def findAllComment(post_id: String = UUID.randomUUID.toString): Seq[Comment] =
    DB readOnly { implicit session =>
      sql"""
         SELECT *
         FROM comment
         WHERE parent_post_id = ${post_id}
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
        .list()
        .apply()
    }

  //コメント先のPostの情報を検索
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
