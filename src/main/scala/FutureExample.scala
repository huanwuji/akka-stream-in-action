import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by huanwuji on 2017/3/26.
  */
object FutureExample extends App {
  def futureTest(): Unit = {
    val fu = Future {
      1
    }
    fu.onComplete {
      case Success(value) ⇒
      case Failure(ex) ⇒
    }
  }
}
