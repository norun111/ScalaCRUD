package models

import java.util.UUID
import java.time.ZonedDateTime
import scalikejdbc._
import java.time.LocalDate

case class Post(id: String = UUID.randomUUID.toString,
                text: String,
                user_id: String,
                comment_count: Int,
                posted_at: LocalDate)

object Post extends SQLSyntaxSupport[Post] {

  import Comment.c
  import nestComment.nc

  def apply(p: ResultName[Post])(rs: WrappedResultSet): Post =
    Post(
      id = rs.string(p.id),
      text = rs.string(p.text),
      user_id = rs.string(p.user_id),
      comment_count = rs.int(p.comment_count),
      posted_at = rs.localDate(p.posted_at)
    )
  def apply(p: SyntaxProvider[Post], rs: WrappedResultSet): Post = apply(p.resultName)(rs)

  val p = Post.syntax("p")

  def findPost(post_id: String = UUID.randomUUID.toString)(
      implicit session: DBSession = autoSession): Option[Post] = {
    withSQL {
      select.from(Post as p).where.eq(p.id, post_id)
    }.map(Post(p.resultName)).single.apply()
  }

  def findAllPost(
      implicit session: DBSession = autoSession): Seq[(Post, Seq[Comment], Seq[nestComment])] = {
    withSQL[Post] {
      select
        .from(Post.as(p))
        .leftJoin(Comment.as(c))
        .on(p.id, c.parent_post_id)
        .leftJoin(nestComment.as(nc))
        .on(c.id, nc.parent_post_id)
    }.one(Post(p.resultName))
      .toManies(
        rs => rs.stringOpt(c.resultName.parent_post_id).map(_ => Comment(c)(rs)),
        rs => rs.stringOpt(nc.resultName.parent_post_id).map(_ => nestComment(nc)(rs)),
      )
      .map((post, comments, nestcomments) => (post, comments, nestcomments))
      .list()
      .apply()
  }

  def create(id: String = UUID.randomUUID.toString, user_id: String, text: String)(
      implicit session: DBSession = autoSession): Unit = {
    withSQL {
      insert.into(Post).values(id, user_id, text, 0, ZonedDateTime.now())
    }.update.apply()
  }

  //親Postのコメント数を+1
  def addCommentCount(post_id: String = UUID.randomUUID.toString) =
    DB autoCommit { implicit session =>
      sql"""UPDATE post SET comment_count = comment_count + 1
  WHERE id = ${post_id}
  """.update().apply()
    }

}
