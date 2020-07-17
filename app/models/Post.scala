package models

import scalikejdbc._
import org.joda.time.DateTime

case class Post(
                    id: Long,
                    userId:  Option[Long] = None,
                    user: Option[User] = None,
                    text: String,
                    commentCount: Int,
                    postedAt: DateTime )

object Post extends SQLSyntaxSupport[Post] {
  def apply(p: SyntaxProvider[Post])(rs: WrappedResultSet): Post = apply(p.resultName)(rs)
  def apply(p: ResultName[Post])(rs: WrappedResultSet): Post = new Post(
    id = rs.get(p.id),
    userId = rs.get(p.userId),
    text = rs.get(p.text),
    commentCount = rs.get(p.commentCount),
    postedAt = rs.get(p.postedAt)
  )

  // join query with company table
  def apply(p: SyntaxProvider[Post], u: SyntaxProvider[User])(rs: WrappedResultSet): Post = {
    apply(p.resultName)(rs).copy(user = rs.longOpt(u.resultName.id).flatMap { _ =>
      if (rs.timestampOpt(u.resultName.deletedAt).isEmpty) Some(User(u)(rs)) else None
    })
  }

  val p = Post.syntax("p")
  private val u = User.u

  def create(name: String, userId: Option[Long] = None, postedAt: DateTime = DateTime.now)(implicit session: DBSession = autoSession): Post = {
    if (userId.isDefined && User.find(userId.get).isEmpty) {
      throw new IllegalArgumentException(s"User is not found. (userId: ${userId})")
    }
    val id = withSQL {
      insert.into(Post).namedValues(
        column.userId -> userId,
        column.text -> text,
        column.commentCount -> commentCount,
        column.postedAt -> postedAt)
    }.updateAndReturnGeneratedKey.apply()

    Post(
      id = id,
      userId = userId.flatMap(id => User.find(id)),
      text = text,
      commentCount = commentCount,
      postedAt = postedAt)
  }
}


