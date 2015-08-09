import java.util.UUID

import com.typesafe.config.ConfigRenderOptions
import models.Product
import play.api.http.HeaderNames._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Filter, RequestHeader, Result, WithFilters}
import play.api.{Application, GlobalSettings}
import util.IndexingTools

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

object Global extends WithFilters(NoCacheFilter) with GlobalSettings {

  override def onStart(app: Application): Unit = {
    val renderer = ConfigRenderOptions.defaults().setComments(false).setOriginComments(false)
    println( s"""
        |
        |
        |Application configuration
        |
        |${play.api.Play.current.configuration.underlying.root().render(renderer)}
        |
        |
        |""".stripMargin)
    ProductDataLoader.loadData()
  }

}

object NoCacheFilter extends Filter {
  override def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    import play.api.libs.concurrent.Execution.Implicits._
    nextFilter(requestHeader).map(_.withHeaders(CACHE_CONTROL -> "no-cache", PRAGMA -> "no-cache"))
  }
}

object ProductDataLoader extends IndexingTools {

  import play.api.libs.concurrent.Execution.Implicits._

  val productList = List("Cellphone", "Tablet", "Laptop")
  
  val r = scala.util.Random

  def aRandomName = Random.shuffle(productList.toList).head


  def getProduct(uniqueId: Int): JsObject = {
    val p = Product(id = uniqueId.toString, price = r.nextInt(100), name = aRandomName, reference = UUID.randomUUID().toString)
    Json.toJson(p).asInstanceOf[JsObject]
  }

  def loadData(): Unit = {
    println("Start generating products")

    val indexResponse = Future.sequence((1 to 1000).map { i =>
      index(es_index, "product", getProduct(i))
    })

    Await.result(indexResponse, 30.seconds)

  }
}
