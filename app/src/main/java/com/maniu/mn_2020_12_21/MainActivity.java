package com.maniu.mn_2020_12_21;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import com.maniu.mn_2020_12_21.proxy_test.InvocationHandlerImpl;
import com.maniu.mn_2020_12_21.proxy_test.ProxyInterface;
import com.maniu.mn_2020_12_21.proxy_test.ProxyTest;
import com.maniu.plugin_core.LoadUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    private void getPluginClass() {
        try {
            LoadUtil loadUtil = new LoadUtil();
            loadUtil.loadClass(getApplicationContext(),"/sdcard/login.apk");

            Class<?> aClass = getClassLoader().loadClass("com.maniu.login.Test");
            Method getToast = aClass.getDeclaredMethod("getToast", Context.class);
            getToast.setAccessible(true);
            getToast.invoke(aClass.newInstance(),getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jumpActivity(String activityName) {
        try {
            //跳转Activity要做的事情
            //1.AMS要检查目的地的Activity是否注册了清单
            //2.AMS要通知ActivityThread来创建目的地的类然后去启动生命周期
            Class<?> aClass = getClassLoader().loadClass(activityName);
            Intent intent = new Intent(this, aClass);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void proxyTest(){
        ProxyTest proxyTest = new ProxyTest();
        //生成一个代理对象
        ProxyInterface proxy = (ProxyInterface) Proxy.newProxyInstance(this.getClassLoader(),
                new Class[]{ProxyInterface.class}, new InvocationHandlerImpl(proxyTest));
        proxy.getLog("我在MainActivity里面调用了getLog()");
    }

    public void jumpLogin(View view) {
        loadPluginApk("/sdcard/login.apk");

//        getPluginClass();
//        proxyTest();
    }

    public void jumpMember(View view) {
        loadPluginApk("/sdcard/member.apk");
    }

    public void loadPluginApk(String apkPath){
        //去加载member的插件
        LoadUtil loadUtil = new LoadUtil();
        loadUtil.loadClass(getApplicationContext(),apkPath);
        //用一个资源对象去管理member模块的资源
        Resources resources = loadUtil.loadPluginResource(getApplicationContext());
        MyApplication myApplication = (MyApplication) getApplication();
        myApplication.setResources(resources);
        //获取到插件的第一个Activity
        ActivityInfo[] activities = loadUtil.getPackageInfo().activities;
        String activityName = activities[0].name;
        //跳转到插件中的Activity里面去
        jumpActivity(activityName);
    }
}
