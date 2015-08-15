package controllers

import _root_.util.IndexingTools
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.mvc.{Controller, Result, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DefaultProductController {
  that: Controller with IndexingTools =>

  val fileName = "data.csv"

  val MAX_RECORDS = 100

  def streamData = Action.async { implicit request =>
    val query =
      """
        | { "query" : { "match_all" : {} } }
      """.stripMargin
    parseFirstScrollResponse(beginSearchWithScroll(es_index, query, 1)).flatMap {
      case (totalCount, scrollid) =>
        streamProductData(toFormatedLine(";"), fileName = fileName, oldScrollId = scrollid, totalCount = totalCount,
          maxCountForFormat = MAX_RECORDS, contentType = "text/csv")
      case _ =>
        Future.successful(Forbidden("Invalid"))
    }
  }

  private def streamProductData(toFormat: List[JsObject] => String, fileName: String, oldScrollId: String, totalCount: Int, maxCountForFormat: Int, contentType: String): Future[Result] = {
    var scrollId = oldScrollId
    var transactionsProcessed = 0

    Future.successful(Ok.chunked {
      Enumerator.generateM {
        parseFollowingScrollResponse(continueSearchWithScroll(scrollId, 1)).flatMap {
          case (_, results, newScrollId) if moreDataRemaining(totalCount, maxCountForFormat, transactionsProcessed, results) =>
            scrollId = newScrollId
            transactionsProcessed = transactionsProcessed + results.length
            Future.successful {
              Some(toFormat(extractProductFromESToList(results)))
            }
          case _ =>
            Future.successful(None)
        }
      }
    }.as(contentType).withHeaders(CONTENT_DISPOSITION -> s"attachment; filename=$fileName"))
  }

  private def moreDataRemaining(totalCount: Int, maxCountForFormat: Int, transactionsProcessed: Int, results: Seq[JsObject]): Boolean = {
    totalCount > 0 && transactionsProcessed + results.length <= maxCountForFormat && transactionsProcessed < totalCount
  }

  private def extractProductFromESToList(response: Seq[JsObject]): List[JsObject] = {
    response.map { json =>
      val __ = json \ "_source"
      Some(__.asInstanceOf[JsObject])
    }.toList.flatten
  }

  private def toFormatedLine(separator: String)(transactions: List[JsObject]) = {
    def toLine(product: JsObject): String = {
      product.fields.map {
        case (name, value) => if (value.isInstanceOf[JsUndefined]) "" else value
      }.mkString(separator)
    }
    if (transactions.nonEmpty)
      transactions.map(toLine).mkString("\n").concat("\n")
    else
      "No products found"
  }
}

object ProductController extends Controller with DefaultProductController with IndexingTools
