package models

import _root_.util.IndexingTools
import com.ning.http.client.Response
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.concurrent.Future
import scala.language.implicitConversions

case class Product(id: String, price: Double, name: String, reference: String)

object Product extends IndexingTools {

  def readNewProduct: Reads[Product] = {
    (
      (__ \ "id").read[String] and
        (__ \ "price").read[Double] and
        (__ \ "name").read[String] and
        (__ \ "reference").read[String]
      )(Product.apply _)
  }

  implicit val productWrite: Writes[Product] = {
    (
      (__ \ "id").write[String] and
        (__ \ "price").write[Double] and
        (__ \ "name").write[String] and
        (__ \ "reference").write[String]
      )(unlift(Product.unapply))
  }

}
