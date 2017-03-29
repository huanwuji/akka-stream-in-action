#akka简介

```scala
val system = ActorSystem()
val actorRef = system.actorOf(Prop[TestActor])
actorRef ! "message"
```
发送过程中到底发生了什么。
首先会调用ActorCell的sendMessage, 然后发送到router或dispatcher上,现在只分析dispatcher上的
Dispatcher.scala
```scala
  protected[akka] def dispatch(receiver: ActorCell, invocation: Envelope): Unit = {
    val mbox = receiver.mailbox
    mbox.enqueue(receiver.self, invocation)
    registerForExecution(mbox, true, false)
  }
```
首先会进入mailbox的队列上，然后通知线程池进行执行
Dispatcher.scala
```scala
  protected[akka] override def registerForExecution(mbox: Mailbox, hasMessageHint: Boolean, hasSystemMessageHint: Boolean): Boolean = {
    if (mbox.canBeScheduledForExecution(hasMessageHint, hasSystemMessageHint)) { //This needs to be here to ensure thread safety and no races
      if (mbox.setAsScheduled()) {
        try {
          executorService execute mbox
          true
        } catch {
          case e: RejectedExecutionException ⇒
            try {
              executorService execute mbox
              true
            } catch { //Retry once
              case e: RejectedExecutionException ⇒
                mbox.setAsIdle()
                eventStream.publish(Error(e, getClass.getName, getClass, "registerForExecution was rejected twice!"))
                throw e
            }
        }
      } else false
    } else false
  }
```
Mailbox.scala
```scala
  @tailrec private final def processMailbox(
    left:       Int  = java.lang.Math.max(dispatcher.throughput, 1),
    deadlineNs: Long = if (dispatcher.isThroughputDeadlineTimeDefined == true) System.nanoTime + dispatcher.throughputDeadlineTime.toNanos else 0L): Unit =
    if (shouldProcessMessage) {
      val next = dequeue()
      if (next ne null) {
        if (Mailbox.debug) println(actor.self + " processing message " + next)
        actor invoke next
        if (Thread.interrupted())
          throw new InterruptedException("Interrupted while processing actor messages")
        processAllSystemMessages()
        if ((left > 1) && ((dispatcher.isThroughputDeadlineTimeDefined == false) || (System.nanoTime - deadlineNs) < 0))
          processMailbox(left - 1, deadlineNs)
      }
    }
```
然后Mailbox就是一个Runable对象，调用Mailbox上的run方法，处理信箱中的消息，然后`actor invoke next`，即ActorCell调用Receive
函数，执行相应的业务处理。
所以我们平时也的class TestActor extend Actor只一个业务逻辑处理函数，send消息时是发送给Dispatcher，Dispatcher会发送给Mailbox
中的队列，然后执行调度来保证一个Mailbox只会被一个线程执行，并且在执行throughput个数的消息之后让出执行线程，给其它的actor逻辑
执行的机会，所以在Actor内是线程安全的，我们可以不用使用同步锁等定义局部变量。通过使用队列来抹平多消息间的并发问题。
好了，简单的actor流程就在这里了。
Dispatcher是调度执行器，Mailbox是消息队列和消息处理者，Actor的具体执行逻辑。

常见问题:
- 如果在actor内使用future就需要注意线程安全问题，因为future函数块中的内容是在另一个线程中执行的。
- 对于阻塞消息的调用，因为akka内部默认的执行线程池是ForkJoinPool，是比较适合执行非阻塞任务的，在非阻塞中需要自己自定义
Dispatcher，`system.actorOf(Props().withDispatcher("block-dispatcher"))`通过指定dispatcher来隔离线程资源，保证非阻塞
业务能正常执行。
- 因为对于同一个actor它是单线程的，当一个actor性能不够时如果利用多线程来提高执行速度。
可以看下[Routing](http://doc.akka.io/docs/akka/2.4/scala/routing.html)的第一个例子。

简单的概念就是这样了，如果需要其它详细的功能请看官方文档。