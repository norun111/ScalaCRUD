package models

import scalikejdbc._
import java.util.Date
import java.util.UUID

import scalikejdbc.config._

case class Post(text: String, comment_count: Int)

object Post {

  DBs.setupAll()

  // no used
  def findAll: Seq[Post] = DB readOnly { implicit session =>
    sql"SELECT id, user_id, text, commented_count, posted_at FROM POST"
      .map { rs =>
        Post(
          rs.string("text"),
          rs.int("comment_count"),
        )
      }
      .list()
      .apply()
  }

  def create(id: String = UUID.randomUUID.toString, text: String, comment_count: Int): Unit =
    DB localTx { implicit session =>
      sql"INSERT INTO post (id, text, comment_count) VALUES (${id},${text},${comment_count})"
        .update()
        .apply()
    }
}
