package models

import models.Post.autoSession
import scalikejdbc._

case class User(id: String, name: String)

object User {}
