import akka.actor.{ActorSystem, Props}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by huanwuji on 2017/3/26.
  */
object FutureExample extends App {
  val system = ActorSystem()
  system.actorOf(Props().withDispatcher("block-dispatcher"))
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
