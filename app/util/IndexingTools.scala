package util

import java.nio.charset.StandardCharsets.UTF_8
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.ning.http.client.Response
import dispatch.Defaults._
import dispatch.{Http, url}
import play.Play
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

trait IndexingTools {

  val config = Play.application.configuration.getConfig("elasticsearch")
  val es_ip = config.getString("ip")
  val es_port = config.getInt("port")
  val host = s"http://$es_ip:$es_port"

  val pattern = config.getString("index.pattern")
  val formatter = DateTimeFormatter.ofPattern(pattern)

  val es_default_type = config.getString("type")

  val es_index : String = {
    val base = config.getString("index.base")
    val formattedDate = formatter.format(ZonedDateTime.now())
    s"$base-product-$formattedDate"
  }

  def index(index: String, `type`: String, body: JsObject): Future[Response] = {
    val req = (url(host) / index / `type`).setBody(Json.stringify(body).getBytes(UTF_8))
    Http(req.POST)
  }

  def parseFirstScrollResponse(response: Future[Response]): Future[(Int, String)] = {
    response.flatMap { r =>
        (Json.parse(r.getResponseBody) \ "error").asOpt[String].map(s => Future.failed(new RuntimeException(s))).getOrElse {
          Future {
            ((Json.parse(r.getResponseBody) \ "hits" \ "total").as[Int], (Json.parse(r.getResponseBody) \ "_scroll_id").as[String])
          }
        }
    }
  }

  def parseFollowingScrollResponse(response: Future[Response]): Future[(Int, Seq[JsObject], String)] = {
    response.flatMap { r =>
        (Json.parse(r.getResponseBody) \ "error").asOpt[String].map(s => Future.failed(new RuntimeException(s))).getOrElse {
          Future {
            ((Json.parse(r.getResponseBody) \ "hits" \ "total").as[Int], (Json.parse(r.getResponseBody) \ "hits" \ "hits").as[Seq[JsObject]], (Json.parse(r.getResponseBody) \ "_scroll_id").as[String])
          }
        }
    }
  }

  def beginSearchWithScroll(index_name: String, query: String, scrollLifetimeInMinutes: Int): Future[Response] = {
    val req = (url(host) / index_name / Option(es_default_type).getOrElse("") / "_search")
      .addQueryParameter("search_type", "scan")
      .addQueryParameter("scroll", s"${scrollLifetimeInMinutes}m")
      .setBody(query.getBytes(UTF_8))
    Http(req.POST)
  }

  def continueSearchWithScroll(scrollId: String, scrollLifetimeInMinutes: Int): Future[Response] = {
    val req = (url(host) / "_search" / "scroll")
      .addQueryParameter("scroll", s"${scrollLifetimeInMinutes}m")
      .addQueryParameter("scroll_id", scrollId)
    Http(req.POST)
  }

}
