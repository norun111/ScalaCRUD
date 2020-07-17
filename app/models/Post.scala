package models

import scalikejdbc._
import org.joda.time.DateTime

case class Post(
                    id: Long,
                    userId:  Option[Long] = None,
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

  val p = Post.syntax("p")
}


