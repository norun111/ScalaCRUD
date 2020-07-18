package models

import scalikejdbc._
import java.util.Date
import scalikejdbc.config._

case class User(id: Long, name: String)

object User {
  def apply(rs: WrappedResultSet) = new User(rs.long("id"), rs.string("name"))
}
