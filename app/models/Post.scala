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

}


