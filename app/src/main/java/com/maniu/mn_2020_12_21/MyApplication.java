package com.maniu.mn_2020_12_21;

import android.app.Application;
import android.content.res.Resources;

import com.maniu.plugin_core.HookUtil;
import com.maniu.plugin_core.ProxyActivity;

public class MyApplication extends Application {
    private Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();
        HookUtil hookUtil = new HookUtil(this, ProxyActivity.class);
        try {
            hookUtil.hookStartActivity();
            hookUtil.hookLaunchActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Resources getResources() {
        //如果插件中的资源对象加载到了  就返回这个插件中的资源对象 否则就返回宿主的资源对象
        return resources == null?super.getResources():resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }
}
