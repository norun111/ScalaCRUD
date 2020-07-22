package models

import scalikejdbc._

case class User(id: String, name: String)

object User {
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

}
