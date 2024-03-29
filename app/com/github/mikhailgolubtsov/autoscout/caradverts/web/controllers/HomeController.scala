package com.github.mikhailgolubtsov.autoscout.caradverts.web.controllers

import controllers.AssetsFinder
import javax.inject._
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit assetsFinder: AssetsFinder)
    extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index = Action {
    Ok("Autoscout car adverts. The app is ready.")
  }

}
