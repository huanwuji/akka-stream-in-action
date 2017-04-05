# akka-stream tcp编程

从我使用的经验来看，这是一个可以和netty相比的tcp处理框架,但是在源码中没看到零拷贝的环节，可能会差些，
从架构上来说是一个很有潜力的tcp框架。
底层是使用akka来进行网络连接处理，外层使用akka-stream封装来简化akka在使用tcp上的复杂性。
[akka-stream关于tcp的官方文档](http://doc.akka.io/docs/akka/2.4/scala/stream/stream-io.html)