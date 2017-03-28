# scala future promise使用介绍与场景

Future.scala
```scala
private[concurrent] object Future {
  class PromiseCompletingRunnable[T](body: => T) extends Runnable {
    val promise = new Promise.DefaultPromise[T]()

    override def run() = {
      promise complete {
        try Success(body) catch { case NonFatal(e) => Failure(e) }
      }
    }
  }

  def apply[T](body: =>T)(implicit executor: ExecutionContext): scala.concurrent.Future[T] = {
    val runnable = new PromiseCompletingRunnable(body)
    executor.prepare.execute(runnable)
    runnable.promise.future
  }
}
```
ExecutionContext即是scala封闭的线程池，使用implicit隐式调用执行
```scala
implicit val executionContext = ExecutionContexts.fromExecutor(Executors.newFixedThreadPool(1))
Future {
  "This is async"
}
```
当body函数被定义，然后就会提交给线程池执行，省略了java的submit操作

Promise
```scala
    def onComplete[U](func: Try[T] => U)(implicit executor: ExecutionContext): Unit = {
      val preparedEC = executor.prepare()
      val runnable = new CallbackRunnable[T](preparedEC, func)
      dispatchOrAddCallback(runnable)
    }
```
通过异步回调CallbackRunnable回调`func: Try[T] => U`方法异步获取执行结果 
```scala
val promise = Promise[Int]()
executeService.submit(new Runable {
  def run() : Unit = {
    promise.success("This a async execute result")
  }
})
promise.onComplete {
  case Success(value) ⇒
  case Failure(ex) ⇒
}
```
Future简化了异步提交任务的过程, Promise通过异步回调来获取异步执行的结果而省略了如java的future.get或scala的Await.result
来阻塞当前线程获取结果，省略了当前线程因为等待结果阻塞而损耗的线程执行时间。所以在纯异步开发中少用Await.result这种语句。