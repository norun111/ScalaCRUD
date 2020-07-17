package controllers

import javax.inject._

import play.api.mvc._

/**
 * Controllerのサンプル実装
 * PlayおよびScalikeJDBCの簡単な使い方サンプルとしてあくまで参考するに留めて下さい。
 * @param cc
 */
@Singleton
class SampleController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * {{{
   *   $ curl 'localhost:9000/index'
   *   hello world
   * }}}
   */
  def index() = Action { Ok("hello world") }

  /***
   * {{{
   *   $ curl 'localhost:9000/database'
       Map(ID -> 1, NAME -> alice, EMAIL -> alice@example.com, CREATED_AT -> 2018-05-01 13:34:07.684, PASSWORD -> password)
   * }}}
   * @return
   */
  def database() = Action {
    import scalikejdbc._

    implicit val session = AutoSession

    val users = sql"SELECT * from users".toMap.list().apply()

    Ok(users.mkString("\n"))
  }
}
