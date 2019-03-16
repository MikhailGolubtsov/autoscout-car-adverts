package com.github.mikhailgolubtsov.autoscout.caradverts

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

/**
 * Functional tests start a Play application internally, available
 * as `app`.
 */
class FunctionalSpec extends PlaySpec with GuiceOneAppPerSuite {

  "HomeController" should {

    "render the index page" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe Status.OK
      contentAsString(home) must include ("Autoscout car adverts. The app is ready.")
    }

  }

}
