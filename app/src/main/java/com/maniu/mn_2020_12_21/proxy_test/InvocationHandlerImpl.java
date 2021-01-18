package com.maniu.mn_2020_12_21.proxy_test;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class InvocationHandlerImpl implements InvocationHandler {
    //要代理的对象
    private ProxyInterface proxy;

    public InvocationHandlerImpl(ProxyInterface proxy) {
        this.proxy = proxy;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        Log.i("cyc", "invoke: "+method.getName());
        //通过动态代理改变原本要打印的信息
        String message = "这是通过动态代理改变之后要打印的信息";
        objects[0] = message;
        Object invoke = method.invoke(proxy, objects);
        return invoke;
    }
}
