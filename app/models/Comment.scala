package models

import java.util.{Date, UUID}

import scalikejdbc._

case class Comment(
                    id: String = UUID.randomUUID.toString,
                    user_id: String,
                    text: String,
                    parent_post_id: String,
                    comment_count: Int,
                    posted_at: Date
                  )

object Comment {

    //コメント先のPostのidとcomment_countの情報を取得
    def findPost(post_id: String): Option[Post] = DB readOnly { implicit session =>
        sql"""
         SELECT id, comment_count
         FROM post
         WHERE id = ${post_id}
      """
          .map { rs =>
              Post(
                  id = rs.string("id"),
              text =  rs.string("text"),
              user_id = rs.string("user_id"),
              comment_count = rs.int("comment_count"),
              posted_at = rs.timestamp("posted_at")
              )
          }
          .single()
          .apply()
    }



    def create(id: String = UUID.randomUUID.toString,
               user_id: String,
               text: String,
               parent_post_id: String,
               comment_count: Int,
               posted_at: Date): Unit =
        DB localTx { implicit session =>
            sql"""INSERT INTO comment
                 (id, user_id, parent_post_id, text, comment_count, posted_at)
                 VALUES
                 (${id},${user_id},${text},${parent_post_id},${comment_count},${posted_at})
                 """
              .update()
              .apply()
        }

    //post_id = form.parent_post_id
    //親Postのコメント数を+1
    def addComment(post_id: String = UUID.randomUUID.toString) =
        DB autoCommit { implicit session =>
        val strPostId = post_id
        sql"""UPDATE post SET comment_count = comment_count + 1
    WHERE post_id = UNHEX(REPLACE(${strPostId}, '-', ''))
    """.update.apply()
    }
}