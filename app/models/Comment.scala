package models

import java.util.{ Date, UUID }

import scalikejdbc._
import scalikejdbc.config._

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

  //コメント先のPostの情報を取得
  def findPost(post_id: String): Option[Post] = DB readOnly { implicit session =>
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

  //値を0で挿入し、その後にpost.comment_count + 1でupdate
  def setCommentCount(post_id: String = UUID.randomUUID.toString) =
    DB autoCommit { implicit session =>
      sql"""UPDATE comment SET comment.comment_count = post.comment_count + 1,
            FROM comment
            INNER JOIN post
            ON ${post_id} = post.id
    """.update().apply()
    }

  //post_id = form.parent_post_id
  //親Postのコメント数を+1
  def addComment(post_id: String = UUID.randomUUID.toString) =
    DB autoCommit { implicit session =>
      val strPostId = post_id
      sql"""UPDATE post SET comment_count = comment_count + 1
    WHERE post_id = UNHEX(REPLACE(${strPostId}, '-', ''))
    """.update().apply()
    }
}
