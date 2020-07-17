package models

import scalikejdbc._

case class User(id: Long, name: String)

object User extends SQLSyntaxSupport[User] {
  override val schemaName = Some("public")

  override val tableName = "user"

  def apply(g: ResultName[User])(rs: WrappedResultSet) = new User(rs.long(g.id), rs.string(g.name))
}