package models

import scalikejdbc._
import java.util.Date
import java.util.UUID
import java.time.ZonedDateTime

import controllers.PostJsonController.{ PostForm, PostIndex }
import scalikejdbc.config._

case class Post(id: String = UUID.randomUUID.toString,
                text: String,
                user_id: String,
                comment_count: Int,
                posted_at: Date)

//SQLInterpolation
object Post extends SQLSyntaxSupport[Post] {

  import Comment.c

  override val columns = Seq("id", "text", "user_id", "comment_count", "posted_at")

  def apply(p: ResultName[Post])(rs: WrappedResultSet): Post =
    Post(
      id = rs.string(p.id),
      text = rs.string(p.text),
      user_id = rs.string(p.user_id),
      comment_count = rs.int(p.comment_count),
      posted_at = rs.date(p.posted_at)
    )
  def apply(p: SyntaxProvider[Post], rs: WrappedResultSet): Post = apply(p.resultName)(rs)

  val p = Post.syntax("p")

  def findPost(post_id: String = UUID.randomUUID.toString)(
      implicit session: DBSession = autoSession): Option[Post] =
    DB readOnly { implicit session =>
      sql"""
         SELECT id, text, user_id, comment_count, posted_at
         FROM post
         WHERE id = ${post_id}
      """
        .map { rs =>
          Post(
            id = rs.string("id"),
            text = rs.string("text"),
            user_id = rs.string("user_id"),
            comment_count = rs.int("comment_count"),
            posted_at = rs.timestamp("posted_at")
          )
        }
        .single()
        .apply()
    }

  def findAllPost(implicit session: DBSession = autoSession) = {
    withSQL[Post] {
      select
        .from(Post.as(p))
        .leftJoin(Comment.as(c))
        .on(p.id, c.parent_post_id)
    }.one(Post(p.resultName))
      .toMany(
        rs => rs.stringOpt(c.resultName.parent_post_id).map(_ => Comment(c)(rs))
      )
      .map((post, comments) => (post, comments)) // 一つのcorporateに対して、userがlistで付いてくる
      .list()
      .apply()
  }

  def create(id: String = UUID.randomUUID.toString, user_id: String, text: String)(
      implicit session: DBSession = autoSession): Unit = {

    withSQL {
      insert.into(Post).values(id, text, user_id, 0, ZonedDateTime.now())
    }.update.apply()
  }
}
