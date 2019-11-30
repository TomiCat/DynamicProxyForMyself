package com.tomi.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class Cat implements Animal {

    @Override
    public void eat() {
        System.out.println("I eat fish");
    }

    @Override
    public int findEat() {
        System.out.println("I find fish");
        return 0;
    }
}
