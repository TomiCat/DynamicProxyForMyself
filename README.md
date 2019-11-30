# DynamicProxyForMyself

### 手写JDK动态代理实现

````
JDK动态代理需要被代理类实现接口，通过接口实现代理接口方法，才能动态生成代理类；
代理类需实现由InvocationHanlder接口，并重写invoke方法；

    本项目将手写JDK动态代理实现；
    接口： Animal；
    被代理类： Cat；
    代理类：   Manager；
    动态代理工具类： DynamicProxy   //对应JDK动态代理工具类Proxy；