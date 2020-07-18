package models

import scalikejdbc._
import scalikejdbc.jsr310._
import java.util.Date

import scalikejdbc.config._

case class Post(id: Long, text: String, comment_count: Int)

object Post {

  DBs.setupAll()

  def findAll: Seq[Post] = DB readOnly { implicit session =>
    sql"SELECT id, text, commented_count, posted_at FROM POST"
      .map { rs =>
        Post(
          rs.long("id"),
          rs.string("text"),
          rs.int("comment_count")
        )
      }
      .list()
      .apply()
  }

  def create(text: String, comment_count: Int): Unit = DB localTx { implicit session =>
    sql"INSERT INTO post (text, comment_count) VALUES (${text},${comment_count})"
      .update()
      .apply()
  }
}

//object Post extends SQLSyntaxSupport[Post] {
//
//  override val tableName = "post"
//
//  override val columns = Seq("id", "user_id", "text", "posted_at", "comment_count")
//
//  def apply(p: SyntaxProvider[Post])(rs: WrappedResultSet): Post = apply(p.resultName)(rs)
//
//  def apply(p: ResultName[Post])(rs: WrappedResultSet): Post = new Post(
//    id = rs.get(p.id),
//    text = rs.string(p.text),
//    userId = rs.get(p.userId),
//    commentCount = rs.int(p.commentCount),
//    posted_at = rs.get(p.postedAt)
//  )
//
////
////  //define alias name of user and post table
////  val p = Post.syntax("p")
////
////  override val autoSession = AutoSession
////
////  def find(id: Long)(implicit session: DBSession = autoSession): Option[Post] = {
////    withSQL {
////      select.from(Post as p).where.eq(p.id, id)
////    }.map(Post(p.resultName)).single.apply()
////  }
////
////  def findAll()(implicit session: DBSession = autoSession): List[Post] = {
////    withSQL(select.from(Post as p)).map(Post(p.resultName)).list.apply()
////  }
//
////  def create(userId: Option[Long] = None,
////             text: String,
////             commentCount: Int,
////             )(implicit session: DBSession = autoSession): Post = {
////    val id = withSQL {
////      insert.into(Post).namedValues(
////          column.userId -> userId,
////          column.text -> text,
////          column.commentCount -> commentCount,
////        )
////    }.updateAndReturnGeneratedKey.apply()
////
////    Post(
////      id = id,
////      userId = userId,
////      text = text,
////      commentCount = commentCount
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
//}
/*
  find by primary key
  def find(id: Long)(implicit session: DBSession = autoSession): Option[Post] =
    withSQL {
      select
        .from(Post as p)
        .leftJoin(User as u)
        .on(p.userId, u.id)
        .where
        .eq(p.id, id)
        .and
        .append(isNotDeleted)
    }.one(Post(p, u))

 */
