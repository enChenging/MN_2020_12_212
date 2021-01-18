package com.maniu.mn_2020_12_21.proxy_test;

import android.util.Log;

public class ProxyTest implements ProxyInterface {
    @Override
    public void getLog(String messager) {
        Log.e("MN------------>",messager);
    }
}
