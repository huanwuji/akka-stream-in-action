# reactive-stream-jvm

[响应式流规范](https://github.com/reactive-streams/reactive-streams-jvm)
GitHub上的规范解释太详细，所以比较复杂，java9里面介绍的更简单一点。

[java 9 reactive api](http://download.java.net/java/jdk9/docs/api/index.html?java/util/concurrent/Flow.html)

[oracle 关于响应式流编程规范文档](https://community.oracle.com/docs/DOC-1006738)

总得来说，这是发布-订阅模式的变种,单发布-订阅模式。

简单介绍一下规范中的四个接口:
```java
//发送数据
public static interface Flow.Publisher<T> {  
    //注册订阅者到发布者接口
    public void    subscribe(Flow.Subscriber<? super T> subscriber);  
}   
  
//接收数据
public static interface Flow.Subscriber<T> {  
    //注册订阅接口
    public void    onSubscribe(Flow.Subscription subscription);  
    //发布者向订阅者发送数据
    public void    onNext(T item) ;  
    //发布者通知订阅者有异常发生
    public void    onError(Throwable throwable) ;  
    //发布者向订阅者通知订阅已完成
    public void    onComplete() ;  
}   
  
//发布者和订阅之间的通讯
public static interface Flow.Subscription {  
    //订阅者向发布者请求数据
    public void    request(long n);  
    //订阅者向发布者通知订阅取消
    public void    cancel() ;  
}   
  
//chain，用于定义发布订阅过程中的事件处理，如果map,filter等的流式api操作
public static interface Flow.Processor<T,R>  extends Flow.Subscriber<T>, Flow.Publisher<R> {  
}  
```