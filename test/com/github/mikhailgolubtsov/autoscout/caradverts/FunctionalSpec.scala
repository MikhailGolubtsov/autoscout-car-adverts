package com.github.mikhailgolubtsov.autoscout.caradverts

import org.scalatest.OptionValues
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

/**
  * Functional tests start a Play application internally, available
  * as `app`.
  */
class FunctionalSpec extends PlaySpec with GuiceOneAppPerTest with OptionValues {

  "HomeController" should {

    "render the index page" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe Status.OK
      contentAsString(home) must include ("Autoscout car adverts. The app is ready.")
    }

  }

  "CarAdvertController" should {

    "create a car advert and get it by id and in a list" in {
      val createRequest = FakeRequest(POST, "/car-adverts").withJsonBody(validJsonCreateRequest)
      val createRequestResponse = route(app, createRequest).value

      status(createRequestResponse) mustBe 200

      val getRequest = FakeRequest(GET, "/car-adverts/8d49c3f5-7637-4528-809f-0bed8f72e549")
      val getRequestResponse = route(app, getRequest).value

      status(getRequestResponse) mustBe 200
      contentAsJson(getRequestResponse) mustBe validJsonCreateRequest

      val getAllRequest = FakeRequest(GET, "/car-adverts?sort=new")
      val getAllResponse = route(app, getAllRequest).value
      status(getAllResponse) mustBe 200
      contentAsJson(getAllResponse) mustBe Json.arr(validJsonCreateRequest)

      val getAllRequestWithSorting = FakeRequest(GET, "/car-adverts?sort=price")
      val getAllResponseWithSorting = route(app, getAllRequestWithSorting).value
      status(getAllResponseWithSorting) mustBe 200
      contentAsJson(getAllResponseWithSorting) mustBe Json.arr(validJsonCreateRequest)
    }

    "create and delete a car advert, should not be found when looked up" in {

      val createRequest = FakeRequest(POST, "/car-adverts").withJsonBody(validJsonCreateRequest)
      val createRequestResponse = route(app, createRequest).value
      status(createRequestResponse) mustBe 200

      val deleteRequest = FakeRequest(DELETE, "/car-adverts/8d49c3f5-7637-4528-809f-0bed8f72e549")
      val deleteRequestResponse = route(app, deleteRequest).value
      status(deleteRequestResponse) mustBe 200

      val getRequest = FakeRequest(GET, "/car-adverts/8d49c3f5-7637-4528-809f-0bed8f72e549")
      val getRequestResponse = route(app, getRequest).value
      status(getRequestResponse) mustBe 404
    }

    "create and update car advert" in {
      val createRequest = FakeRequest(POST, "/car-adverts").withJsonBody(validJsonCreateRequest)
      val createRequestResponse = route(app, createRequest).value

      status(createRequestResponse) mustBe 200

      val updateRequest = FakeRequest(PUT, "/car-adverts/8d49c3f5-7637-4528-809f-0bed8f72e549")
        .withJsonBody(validJsonUpdateRequest)
      val updateRequestResponse = route(app, updateRequest).value
      status(updateRequestResponse) mustBe 200

      val getRequest = FakeRequest(GET, "/car-adverts/8d49c3f5-7637-4528-809f-0bed8f72e549")
      val getRequestResponse = route(app, getRequest).value

      status(getRequestResponse) mustBe 200
      contentAsJson(getRequestResponse) mustBe Json.parse(
        """
          |{
          |  "id": "8d49c3f5-7637-4528-809f-0bed8f72e549",
          |  "title": "Audi",
          |  "fuel": "gasoline",
          |  "price": 15000,
          |  "new": true
          |}
          |""".stripMargin)
    }
  }

  val validJsonCreateRequest = Json.parse(
    """
      |{
      |  "id": "8d49c3f5-7637-4528-809f-0bed8f72e549",
      |  "title": "Audi",
      |  "fuel": "gasoline",
      |  "price": 10000,
      |  "new": true
      |}
      |""".stripMargin
  )

  val validJsonUpdateRequest = Json.parse(
    """
      |{
      |  "title": "Audi",
      |  "fuel": "gasoline",
      |  "price": 15000,
      |  "new": true
      |}
      |""".stripMargin
  )
}
