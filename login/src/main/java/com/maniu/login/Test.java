package com.maniu.login;


import android.content.Context;
import android.widget.Toast;

public class Test {
    public void getToast(Context context){
        Toast.makeText(context,"我是插件中的类方法，我被加载并调用了",Toast.LENGTH_LONG).show();
    }
}
