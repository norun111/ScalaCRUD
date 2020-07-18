package models

import scalikejdbc._
import java.util.Date
import scalikejdbc.config._

case class User(id: Long, name: String)

case class Post(id: Long,
                text: String,
                userId: Option[Long] = None,
                commentCount: Int,
                posted_at: Date,
                user: Option[User] = None,
)

object User {
  def apply(rs: WrappedResultSet) = new User(rs.long("id"), rs.string("name"))
}

object Post {
  def apply(rs: WrappedResultSet) = new Post(
    rs.long("id"),
    rs.string("text"),
    rs.longOpt("user_id"),
    rs.int("commentCount"),
    rs.date("posted_at")
  )

  def withUser(rs: WrappedResultSet) = {
    new Post(
      id = rs.long("p_id"),
      text = rs.string("p_text"),
      userId = rs.longOpt("p_user_id"),
      commentCount = rs.int("commentCount"),
      posted_at = rs.date("posted_at"),
      user = rs.longOpt("p_id").map(id => User(id, rs.string("u_name")))
    )
  }

  def addPost(text: String): Unit = {
    DBs.setupAll()

    val count = DB autoCommit { implicit session =>
      sql"insert into post (name, created_at) values (${text}, current_date())".update.apply()
    }

    DBs.closeAll()
  }

  def execute(): Unit = {
    // loaded from "db.default.*"
    val postIds = DB readOnly { implicit session =>
      sql"select id from public.post".map(_.long(1)).list.apply()
    }

  }
}
//object User extends SQLSyntaxSupport[User] {
//
//  override val tableName = "user"
//
////  def apply(u: ResultName[User])(rs: WrappedResultSet): User = {
////    new User(
////      rs.long(u.id),
////      rs.string(u.name)
////    )
////  }
//}
//
//
//
//object Post extends SQLSyntaxSupport[Post] {
//
//  override val tableName = "post"
////  override val columns = Seq("id", "user_id", "text", "posted_at", "comment_count")
//
//  //define alias name of user and post table
////  val p = this.syntax("p")
//
////  def apply(p: SyntaxProvider[Post])(rs: WrappedResultSet): Post = apply(p.resultName)(rs)
//
////  def apply(p: ResultName[Post], us: ResultName[User])(rs: WrappedResultSet): Post = {
////
////    val uid = rs.longOpt(us.id)
////    Post(
////      rs.long(p.id),
////      rs.string(p.text),
////      userId = uid,
////      user = uid.map(_ => User(us)(rs)),
////      rs.int(p.commentCount),
////      rs.zonedDateTime(p.postedAt)
////    )
////  }
//
////   join query with company table
////    def apply(p: SyntaxProvider[Post], u: SyntaxProvider[User])(rs: WrappedResultSet): Post = {
////      apply(p.resultName)(rs).copy(user = rs.longOpt(u.resultName.id).flatMap { _ =>
////        if (rs.timestampOpt(u.resultName.deletedAt).isEmpty) Some(User(u)(rs)) else None
////      })
////    }
//
////
////  def opt(m: SyntaxProvider[Post])(rs: WrappedResultSet): Option[Post] =
////    rs.longOpt(m.resultName.id).map(_ => Post(m)(rs))
////
//
////  def create(userId: Option[Long] = None,
////             text: String,
////             commentCount: Int,
////             postedAt: ZonedDateTime = ZonedDateTime.now)(
////      implicit session: DBSession = autoSession): Post = {
//////        if (userId.isDefined && User.find(userId.get).isEmpty) {
//////          throw new IllegalArgumentException(s"User is not found. (userId: ${userId})")
//////        }
////    val id = withSQL {
////      insert
////        .into(Post)
////        .namedValues(
////          column.userId -> userId,
////          column.text -> text,
////          column.commentCount -> commentCount,
////          column.postedAt -> postedAt
////        )
////    }.updateAndReturnGeneratedKey.apply()
////
////    Post(
////      id = id,
////      userId = userId,
////      text = text,
////      commentCount = commentCount,
////      postedAt = postedAt
////    )
////  }
//  // find all members
//}
////  // find by primary key
//////  def find(id: Long)(implicit session: DBSession = autoSession): Option[Post] =
//////    withSQL {
//////      select
//////        .from(Post as p)
//////        .leftJoin(User as u)
//////        .on(p.userId, u.id)
//////        .where
//////        .eq(p.id, id)
//////        .and
//////        .append(isNotDeleted)
//////    }.one(Post(p, u))
