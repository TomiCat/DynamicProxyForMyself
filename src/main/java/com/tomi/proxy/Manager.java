package com.tomi.proxy;

import java.lang.reflect.Method;

public class Manager implements MyInvocationHandler {

    private Object target;

    public Object getInstance(Object target) {
        this.target = target;
        return DynamicProxy.newProxyInstance(new ProxyClassLoader(), target.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object object, Method method, Object[] args) {
        try {
            before();
            Object result = method.invoke(this.target, args);
            after();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void before(){
        System.out.println("before");
    }

    public void after(){
        System.out.println("after");
    }

    public static void main(String[] args) {
        Animal cat = (Animal) new Manager().getInstance(new Cat());
        cat.findEat();
    }
}
