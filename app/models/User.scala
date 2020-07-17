package models

import scalikejdbc._

case class User(id: Long, name: String)

object User extends SQLSyntaxSupport[User] {

  override val tableName = "user"

  def apply(u: ResultName[User])(rs: WrappedResultSet) = new User(rs.long(u.id), rs.string(u.name))
}
