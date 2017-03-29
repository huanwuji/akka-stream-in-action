import java.util.concurrent.{Callable, Executors}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by huanwuji on 2017/3/23.
  */
object MultiThreadExample extends App {

  def syncMethod(): Unit = {
    var count = 0
    for (i ← 1 to 10) {
      count += 1
    }
    println(count)
  }

  def asyncMethod(): Unit = {
    val executor = Executors.newFixedThreadPool(2)
    var count = 0
    val future1 = executor.submit(new Callable[Int] {
      override def call(): Int = {
        var count1 = 0
        for (i ← 1 to 10) {
          count1 += 1
        }
        count1
      }
    })
    count = future1.get //获取结果是阻塞的
    println(count)
  }

  def asyncNonblockMethod(): Unit = {
    val fu = Future {
      var count1 = 0
      for (i ← 1 to 10) {
        count1 += 1
      }
      count1
    }
    fu.onSuccess { case count ⇒ println(count) } //通过函数式回调避免线程阻塞
  }
}
