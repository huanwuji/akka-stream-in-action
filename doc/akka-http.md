# akka-http
这是一个纯异步http处理工具集，以工具类的方式。
架构上，HttpRequest => HttpResponse函数来定义，并通过compose这种函数层层调用方式来实现服务器。
比较适合于高性能http后端服务使用。