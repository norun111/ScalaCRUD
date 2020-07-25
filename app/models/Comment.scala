package models

import java.time.{ LocalDate, LocalDateTime, ZonedDateTime }
import java.util._

import scalikejdbc._

case class nestComment(
    id: String = UUID.randomUUID.toString,
    user_id: String,
    text: String,
    parent_post_id: String,
    comment_count: Int,
    posted_at: Date
)

object nestComment extends SQLSyntaxSupport[nestComment] {
  
  var nc = nestComment.syntax("nc")

  override val tableName = "comment"

  override val columns =
    Seq("id", "user_id", "text", "parent_post_id", "comment_count", "posted_at")

  def apply(nc: ResultName[nestComment])(rs: WrappedResultSet): nestComment = new nestComment(
    id = rs.string(nc.id),
    user_id = rs.string(nc.user_id),
    text = rs.string(nc.text),
    parent_post_id = rs.string(nc.parent_post_id),
    comment_count = rs.int(nc.comment_count),
    posted_at = rs.date(nc.posted_at)
  )
  def apply(nc: SyntaxProvider[nestComment])(rs: WrappedResultSet): nestComment =
    apply(nc.resultName)(rs)
}

case class Comment(
    id: String = UUID.randomUUID.toString,
    user_id: String,
    text: String,
    parent_post_id: String,
    comment_count: Int,
    posted_at: LocalDate
)

object Comment extends SQLSyntaxSupport[Comment] {

  def apply(c: ResultName[Comment])(rs: WrappedResultSet): Comment = new Comment(
    id = rs.string(c.id),
    user_id = rs.string(c.user_id),
    text = rs.string(c.text),
    parent_post_id = rs.string(c.parent_post_id),
    comment_count = rs.int(c.comment_count),
    posted_at = rs.localDate(c.posted_at)
  )
  def apply(c: SyntaxProvider[Comment])(rs: WrappedResultSet): Comment = apply(c.resultName)(rs)

  var c = Comment.syntax("c")

  def findAllComment(post_id: String = UUID.randomUUID.toString)(implicit session: DBSession =
                                                                   autoSession): Seq[Comment] = {
    withSQL(select.from(Comment as c).where.eq(c.parent_post_id, post_id))
      .map(Comment(c.resultName))
      .list
      .apply()
  }

  //コメント先のコメントを検索
  def findComment(comment_id: String = UUID.randomUUID.toString)(implicit session: DBSession =
                                                                   autoSession): Option[Comment] = {
    withSQL {
      select.from(Comment as c).where.eq(c.id, comment_id)
    }.map(Comment(c.resultName)).single.apply()
  }

  def create(id: String = UUID.randomUUID.toString,
             user_id: String,
             text: String,
             parent_post_id: String = UUID.randomUUID.toString)(implicit session: DBSession =
                                                                  autoSession): Unit = {
    withSQL {
      insert.into(Comment).values(id, user_id, text, parent_post_id, 0, ZonedDateTime.now())
    }.update.apply()
  }

  //親Commentのコメント数を+1
  def addCommentCount(comment_id: String = UUID.randomUUID.toString) =
    DB autoCommit { implicit session =>
      sql"""UPDATE comment SET comment_count = comment_count + 1
    WHERE id = ${comment_id}
    """.update().apply()
    }

}
