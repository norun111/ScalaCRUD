package models

import java.time.ZonedDateTime
import scalikejdbc._

case class Post(id: Long,
                userId: Option[Long] = None,
                user: Option[User] = None,
                text: String,
                commentCount: Int,
                postedAt: ZonedDateTime)

object Post extends SQLSyntaxSupport[Post] {
  def apply(p: SyntaxProvider[Post])(rs: WrappedResultSet): Post = apply(p.resultName)(rs)
  def apply(p: ResultName[Post])(rs: WrappedResultSet): Post = new Post(
    id = rs.long(p.id),
    userId = rs.longOpt(p.userId),
    text = rs.string(p.text),
    commentCount = rs.int(p.commentCount),
    postedAt = rs.zonedDateTime(p.postedAt)
  )

  // join query with company table
  def apply(p: SyntaxProvider[Post], u: SyntaxProvider[User])(rs: WrappedResultSet): Post = {
    apply(p.resultName)(rs).copy(user = rs.longOpt(u.resultName.id).flatMap { _ =>
      if (rs.timestampOpt(u.resultName.deletedAt).isEmpty) Some(User(u)(rs)) else None
    })
  }
  private val u = User.u
  val p = Post.syntax("p")

  // find by primary key
//  def find(id: Long)(implicit session: DBSession = autoSession): Option[Post] =
//    withSQL {
//      select
//        .from(Post as p)
//        .leftJoin(User as u)
//        .on(p.userId, u.id)
//        .where
//        .eq(p.id, id)
//        .and
//        .append(isNotDeleted)
//    }.one(Post(p, u))

  def create(userId: Option[Long] = None,
             text: String,
             commentCount: Int,
             postedAt: ZonedDateTime = ZonedDateTime.now)(implicit session: DBSession = autoSession): Post = {
    if (userId.isDefined && User.find(userId.get).isEmpty) {
      throw new IllegalArgumentException(s"User is not found. (userId: ${userId})")
    }
    val id = withSQL {
      insert
        .into(Post)
        .namedValues(
          column.userId -> userId,
          column.text -> text,
          column.commentCount -> commentCount,
          column.postedAt -> postedAt
        )
    }.updateAndReturnGeneratedKey.apply()

    Post(
      id = id,
      userId = userId.flatMap(id => User.find(id)),
      text = text,
      commentCount = commentCount,
      postedAt = postedAt
    )
  }
}
