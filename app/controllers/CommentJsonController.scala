package controllers

import javax.inject.Inject
import play.api.mvc.{ AbstractController, ControllerComponents }

class CommentJsonController @Inject()(components: ControllerComponents)
    extends AbstractController(components) {}
