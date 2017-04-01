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
    public void    onNext(T item) ;  
    public void    onError(Throwable throwable) ;  
    public void    onComplete() ;  
}   
  
//发布者和订阅之间的通讯
public static interface Flow.Subscription {  
    public void    request(long n);  
    public void    cancel() ;  
}   
  
//chain
public static interface Flow.Processor<T,R>  extends Flow.Subscriber<T>, Flow.Publisher<R> {  
}  
```