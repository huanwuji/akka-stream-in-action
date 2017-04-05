# akka-stream 介绍

akka-stream是reactive-stream规范的实现，所以遵循backpressure，异步响应式规范。
可以很好的解决生产者产生数据的速度大于消费者的情况。
并且因为数据流之间是异步处理，所以可以充分的利用多线程的能力。
底层是以akka做为解释器，所以有高性能的并发处理能力。
总之是一个轻量级，高性能单进程响应式流框架。

akka-stream结构:
akka-stream的数据流整体设计成流的形式。
结构上会比响应式流定义的更多，拿来和响应式流对照的话。
source -> publisher
sink -> subscriber
flow -> processer
所以整体分为三个组件，但底层都是表示为shape
```scala
abstract class Shape {
  /**
   * Scala API: get a list of all input ports
   */
  def inlets: immutable.Seq[Inlet[_]]

  /**
   * Scala API: get a list of all output ports
   */
  def outlets: immutable.Seq[Outlet[_]]
```
SourceShape是没有inlets只有outlets的shape.
SinkShape是没有outlets的只有inlets的shape.
FlowShape则是两个都存在。

Shape及形状,把多个形状连接下来就构成边，Inlets，outlets代表一个形状的入口和出口，很多个形状构成一个图。
GraphStage用来定义从上一个图形到下一个图形之间的数据处理逻辑，通过setHandler来定义。
拿一个简单的map来说，
```scala
final case class Map[In, Out](f: In ⇒ Out) extends GraphStage[FlowShape[In, Out]] {
  val in = Inlet[In]("Map.in")
  val out = Outlet[Out]("Map.out")
  override val shape = FlowShape(in, out)
  //定义了一些形状需要的属性，如容错，dispatcher等。
  override def initialAttributes: Attributes = DefaultAttributes.map

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    //处理逻辑
    new GraphStageLogic(shape) with InHandler with OutHandler {
      private def decider =
        inheritedAttributes.get[SupervisionStrategy].map(_.decider).getOrElse(Supervision.stoppingDecider)

      override def onPush(): Unit = {
        try {
          push(out, f(grab(in)))
        } catch {
          case NonFatal(ex) ⇒ decider(ex) match {
            case Supervision.Stop ⇒ failStage(ex)//如此定义会把异常向下游传递
            case _                ⇒ pull(in)
          }
        }
      }

      override def onPull(): Unit = pull(in)

      setHandlers(in, out, this)
    }
}
```
它的shape和reactive-stream规范是大同小异的。虽然我们面对的实现接口不一样，但是akka-stream可以接入reactive-stream的实现类。
akka-stream自定义了一些Source,Sink,Flow的实现。可以支持merge,balance,map,filter，还支持对单个逻辑的多线程处理，如mapAsync
实现数据的并发转换。并且shape之间通过async来确认图形的异步边界保证并行处理，内部指定ActorAttributes.Dispatcher，做用于
Shape内部的多线程处理。GraphStage内部还定义了AsyncCallback和StageActor(一个简单的用于GraphStage内部使用的actor)等很多种手段
保证流内部的异步处理。

如果我们需要自己编写GraphStage，简单的介绍下。
```scala
trait InHandler {
  //推
  @throws(classOf[Exception])
  def onPush(): Unit

  //上游完成
  @throws(classOf[Exception])
  def onUpstreamFinish(): Unit = GraphInterpreter.currentInterpreter.activeStage.completeStage()

  //上流执行失败
  @throws(classOf[Exception])
  def onUpstreamFailure(ex: Throwable): Unit = GraphInterpreter.currentInterpreter.activeStage.failStage(ex)
}

trait OutHandler {
  //拉
  @throws(classOf[Exception])
  def onPull(): Unit
  //通知下流完成
  @throws(classOf[Exception])
  def onDownstreamFinish(): Unit = {
    GraphInterpreter
      .currentInterpreter
      .activeStage
      .completeStage()
  }
}
```
简单来说，这就是一个拉-推的过程，通过调用拉通知上游拉取数据，然后上游给下游push数据。