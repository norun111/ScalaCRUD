package models

import scalikejdbc._
import java.util.Date
import java.util.UUID

import scalikejdbc.config._

case class Post(id: String = UUID.randomUUID.toString,
                text: String,
                user_id: String,
                comment_count: Int,
                posted_at: Date)

//SQLInterpolation
object Post {

  DBs.setupAll()

  def findUser(user_id: String): Option[User] = DB readOnly { implicit session =>
    sql"""
         SELECT id, name
         FROM user
         WHERE id = ${user_id}
      """
      .map { rs =>
        User(
          id = rs.string("id"),
          name = rs.string("name")
        )
      }
      .single()
      .apply()
  }

  def create(id: String = UUID.randomUUID.toString,
             user_id: String,
             text: String,
             comment_count: Int,
             posted_at: Date): Unit =
    DB localTx { implicit session =>
      sql"""INSERT INTO post (id, user_id, text, comment_count, posted_at) VALUES (${id},${user_id},${text},${comment_count},${posted_at})"""
        .update()
        .apply()
    }
}
