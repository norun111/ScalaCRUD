package models

import scalikejdbc._
import org.joda.time.DateTime

case class Post(
                    id: Long,
                    userId:  Option[Long],
                    text: String,
                    commentCount: Int,
                    postedAt: DateTime ) {

  def save()(implicit session: DBSession = Post.autoSession): Post.save(this)(session)
}


