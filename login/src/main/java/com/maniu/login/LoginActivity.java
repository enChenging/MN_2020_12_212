package com.maniu.login;


import android.os.Bundle;
import android.util.Log;

import com.maniu.plugin_core.BaseActivity;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ClassLoader classLoader = getClassLoader();
        Log.e("MN---->",classLoader.getClass().getName());
    }
}
