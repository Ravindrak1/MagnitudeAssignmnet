package simulations

import io.gatling.core.Predef._ // required for gatling core structure DSL
import io.gatling.http.Predef._ //required for Gatling HTTPS DSL

import scala.concurrent.duration._ //used for specifying duration unit eg "10 sec"

class petstoreProductBookingTest extends Simulation
{

  val httpProtocol = http
    .baseUrl("https://petstore.octoperf.com")
    .inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.9")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")

  val headers_0 = Map(
    "sec-ch-ua" -> """ Not;A Brand";v="99", "Google Chrome";v="91", "Chromium";v="91""",
    "sec-ch-ua-mobile" -> "?0",
    "sec-fetch-dest" -> "document",
    "sec-fetch-mode" -> "navigate",
    "sec-fetch-site" -> "none",
    "sec-fetch-user" -> "?1")

  val headers_1 = Map(
    "sec-ch-ua" -> """ Not;A Brand";v="99", "Google Chrome";v="91", "Chromium";v="91""",
    "sec-ch-ua-mobile" -> "?0",
    "sec-fetch-dest" -> "document",
    "sec-fetch-mode" -> "navigate",
    "sec-fetch-site" -> "same-origin",
    "sec-fetch-user" -> "?1")

  val headers_3 = Map(
    "origin" -> "https://petstore.octoperf.com",
    "sec-ch-ua" -> """ Not;A Brand";v="99", "Google Chrome";v="91", "Chromium";v="91""",
    "sec-ch-ua-mobile" -> "?0",
    "sec-fetch-dest" -> "document",
    "sec-fetch-mode" -> "navigate",
    "sec-fetch-site" -> "same-origin",
    "sec-fetch-user" -> "?1")

 val csvFeeder = csv("Data/DataFile.csv")

  val scn = scenario("PetStoreTestClass")

    .exec(flushHttpCache)
    .exec(flushSessionCookies)
    .exec(flushCookieJar)

    .exec(http("request_0")
      .get("/")
      .headers(headers_0))
    .pause(4)

    .exec(http("EnterTheStore")
      .get("/actions/Catalog.action")
      .headers(headers_1))
    .pause(6)

    .exec(http("ClickOnSignIn")
      .get("/actions/Account.action?signonForm=")
      .headers(headers_1)
      .check(regex("""_fp" value="(.*?)" />""").exists.saveAs("_fpVal"))
      .check(regex("""_sourcePage" value="(.*?)" />""").exists.saveAs("_sourcePage")))
    .pause(6)

    .feed(csvFeeder)
    .exec(http("Login")
      .post("/actions/Account.action")
      .headers(headers_3)
      .formParam("username", "${p_Userid}")
      .formParam("password", "${p_Password}")
      .formParam("signon", "Login")
      .formParam("_sourcePage", "${_sourcePage}")
      .formParam("__fp", "${_fpVal}")
      .check(regex("""_fp" value="(.*?)" />""").exists.saveAs("_fpVal")))
    .pause(9)

    .exec{
      session => println(session("p_Userid").as[String])
        session
    }

    .repeat(2)
    {
      exec(http("SelectPet")
        .get("/actions/Catalog.action?viewCategory=&categoryId=FISH")
        .headers(headers_1))
        .pause(3)

        .exec(http("SelectPetType")
          .get("/actions/Catalog.action?viewProduct=&productId=FI-SW-01")
          .headers(headers_1))
        .pause(3)

        .exec(http("SelectPetSubType")
          .get("/actions/Catalog.action?viewItem=&itemId=EST-1")
          .headers(headers_1))
        .pause(2)

        .exec(http("AddItemToCart")
          .get("/actions/Cart.action?addItemToCart=&workingItemId=EST-1")
          .headers(headers_1)
          .check(regex("""_fp" value="(.*?)" />""").exists.saveAs("_fpVal1"))
          .check(regex("""_sourcePage" value="(.*?)" />""").exists.saveAs("_sourcePage1")))
        .pause(4)
    }

    .exec(http("UpdateCart")
      .post("/actions/Cart.action")
      .headers(headers_3)
      .formParam("EST-1", "3")
      .formParam("EST-17", "2")
      .formParam("updateCartQuantities", "Update Cart")
      .formParam("_sourcePage", "${_sourcePage1}")
      .formParam("__fp", "${_fpVal1}"))
    .pause(3)

    .exec(http("PorceedToCheckOut")
      .get("/actions/Order.action?newOrderForm=")
      .headers(headers_1)
      .check(regex("""_fp" value="(.*?)" />""").exists.saveAs("_fpVal2"))
      .check(regex("""_sourcePage" value="(.*?)" />""").exists.saveAs("_sourcePage2")))
    .pause(2)

    .exec(http("Continue")
      .post("/actions/Order.action")
      .headers(headers_3)
      .formParam("order.cardType", "Visa")
      .formParam("order.creditCard", "999 9999 9999 9999")
      .formParam("order.expiryDate", "12/03")
      .formParam("order.billToFirstName", "ABC")
      .formParam("order.billToLastName", "XYX")
      .formParam("order.billAddress1", "901 San Antonio Road")
      .formParam("order.billAddress2", "MS UCUP02-206")
      .formParam("order.billCity", "Palo Alto")
      .formParam("order.billState", "CA")
      .formParam("order.billZip", "94303")
      .formParam("order.billCountry", "USA")
      .formParam("newOrder", "Continue")
      .formParam("_sourcePage", "${_sourcePage2}")
      .formParam("__fp", "${_fpVal2}"))
    .pause(2)

    .exec(http("Confirm")
      .get("/actions/Order.action?newOrder=&confirmed=true")
      .headers(headers_1))

  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}