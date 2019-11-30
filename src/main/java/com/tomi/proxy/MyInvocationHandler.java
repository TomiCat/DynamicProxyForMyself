package com.tomi.proxy;

import java.lang.reflect.Method;

public interface MyInvocationHandler {
    public Object invoke(Object object, Method method, Object[] args);
}
