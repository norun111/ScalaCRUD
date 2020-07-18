package models

import scalikejdbc._
import java.util.Date
import scalikejdbc.config._

case class User(id: Long, name: String)


object User {
  def apply(rs: WrappedResultSet) = new User(rs.long("id"), rs.string("name"))
}

//object Post {
//  def apply(rs: WrappedResultSet) = new Post(
//    rs.long("id"),
//    rs.string("text"),
//    rs.longOpt("user_id"),
//    rs.int("commentCount"),
//    rs.date("posted_at")
//  )
//
//  def withUser(rs: WrappedResultSet) = {
//    new Post(
//      id = rs.long("p_id"),
//      text = rs.string("p_text"),
//      userId = rs.longOpt("p_user_id"),
//      commentCount = rs.int("commentCount"),
//      posted_at = rs.date("posted_at"),
//      user = rs.longOpt("p_id").map(id => User(id, rs.string("u_name")))
//    )
//  }
//
//  def addPost(text: String): Unit = {
//    DBs.setupAll()
//
//    val count = DB autoCommit { implicit session =>
//      sql"insert into post (name, created_at) values (${text}, current_date())".update.apply()
//    }
//
//    DBs.closeAll()
//  }
//
//  def execute(): Unit = {
//    // loaded from "db.default.*"
//    val postIds = DB readOnly { implicit session =>
//      sql"select id from public.post".map(_.long(1)).list.apply()
//    }
//
//  }
//}
