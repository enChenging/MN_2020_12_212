package com.maniu.plugin_core;

import android.content.res.Resources;

import androidx.appcompat.app.AppCompatActivity;

//插件是在宿主的一部分
public class BaseActivity extends AppCompatActivity {
    @Override
    public Resources getResources() {
        if(getApplication()!=null && getApplication().getResources()!=null){
            return getApplication().getResources();
        }
        return super.getResources();
    }
}
