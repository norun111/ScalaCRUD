package models

import java.time.ZonedDateTime
import java.util.{ Date, UUID }

import controllers.CommentJsonController.CommentIndex
import scalikejdbc._

case class Comment(
    id: String = UUID.randomUUID.toString,
    user_id: String,
    text: String,
    parent_post_id: String,
    comment_count: Int,
    posted_at: Date
)

//object CommentIndex extends SQLSyntaxSupport[CommentIndex] {
//
//  def apply(cI: ResultName[CommentIndex])(rs: WrappedResultSet): CommentIndex = new CommentIndex(
//    id = rs.string(cI.id),
//    user_id = rs.string(cI.user_id),
//    text = rs.string(cI.text),
//    parent_post_id = rs.string(cI.parent_post_id),
//    comment_count = rs.int(cI.comment_count),
//    posted_at = rs.date(cI.posted_at)
//  )
//
//  def apply(c: SyntaxProvider[CommentIndex])(rs: WrappedResultSet): CommentIndex =
//    apply(c.resultName)(rs)
//  var cI = CommentIndex.syntax("cI")
//
//}

object Comment extends SQLSyntaxSupport[Comment] {

  def apply(c: ResultName[Comment])(rs: WrappedResultSet): Comment = new Comment(
    id = rs.string(c.id),
    user_id = rs.string(c.user_id),
    text = rs.string(c.text),
    parent_post_id = rs.string(c.parent_post_id),
    comment_count = rs.int(c.comment_count),
    posted_at = rs.date(c.posted_at)
  )
  def apply(c: SyntaxProvider[Comment])(rs: WrappedResultSet): Comment = apply(c.resultName)(rs)

  var c = Comment.syntax("c")

  def findAllComment(post_id: String = UUID.randomUUID.toString): Seq[CommentIndex] =
    DB readOnly { implicit session =>
      sql"""
         SELECT *
         FROM comment
         WHERE parent_post_id = ${post_id}
      """
        .map { rs =>
          CommentIndex(
            id = rs.string("id"),
            user_id = rs.string("user_id"),
            text = rs.string("text"),
            parent_post_id = rs.string("parent_post_id"),
            comment_count = rs.int("comment_count"),
            posted_at = rs.timestamp("posted_at")
          )
        }
        .list()
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
  def addCommentCountOnComment(comment_id: String = UUID.randomUUID.toString) =
    DB autoCommit { implicit session =>
      sql"""UPDATE comment SET comment_count = comment_count + 1
    WHERE id = ${comment_id}
    """.update().apply()
    }

}
