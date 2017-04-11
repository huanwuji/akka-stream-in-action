# akka-stream 应用场景

前面介绍了akka-stream tcp和akka-http，都是异步非阻塞的网络服务器，所以可以用在绝大多数的高性能网络调用上。
如果高性能的后台服务端，高性能API调用，后台的网络传输通讯。

最主要的就是在数据处理上。
比如根据一批用户id来调用单个接口获取数据
正常的伪代码:
```scala
for(userId ← userIds) {
  list.add(rest.get(userId))
}
list
```
如果是akka-stream
```scala
source(userIds).map(rest.get(userId))
```
如果需要使用多线程加速
```scala
source(userIds).mapAsyncUnOrder(rest.get(userId))
```
如果是每5个做1个批量
```scala
source(userIds).grouped(5).mapAsyncUnOrder(rest.get(userIds))
```
如果需要把它同时写文件和数据库
```scala
val brocast = Broadcast[User](2)
source(userIds).grouped(5).mapAsyncUnOrder(rest.get(userIds)) ~> brocast
brocast(0) ~> jdbcSink
brocast(1) ~> fileSink
```
这些只是简单的例子，只是为了介绍我们只要封装了基本的代码逻辑，就可以通过akka-stream完成
很多流式的操作，重要的是它可以异步，异步，随意的加特效。
有兴趣可以看下我的teleporter项目，就是封装了各种组件的基本操作，然后就可以利用akka-stream随意
的对数据处理的各个环节加特效。