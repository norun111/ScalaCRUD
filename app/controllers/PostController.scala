package controllers

import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ AbstractController, ControllerComponents }
import scalikejdbc._
import play.api.data._
import play.api.data.Forms._
import models._
@Singleton
class PostController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index = TODO

  def create = TODO
}
