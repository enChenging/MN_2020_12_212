package com.maniu.member;

import android.os.Bundle;
import android.util.Log;

import com.maniu.plugin_core.BaseActivity;

public class MemberActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        Log.e("MN-------->",getResources().getString(R.string.app_name));
    }
}
