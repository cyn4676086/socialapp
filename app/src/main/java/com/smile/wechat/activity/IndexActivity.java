package com.smile.wechat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import com.smile.wechat.AppConst;
import com.smile.wechat.R;


public class IndexActivity extends AppCompatActivity {

    /**
     * 1.把启动页全屏
     * 2.延迟进入主页
     * 3.根据具体逻辑是进入主页还是引导页还是登录页
     * 4.适配刘海屏
     */


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                SharedPreferences sp=getSharedPreferences(AppConst.SP_IS_FIRST_APP,MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                boolean isFirst=sp.getBoolean(AppConst.SP_IS_FIRST_APP,true);
                if (msg.what == 1) {
                    // 若已存在登陆信息则直接打开主页面，进入自动登陆流程，在主页面通过判断用户状态决定下一步操作
                        if (isFirst) {
                            //跳转到引导页
                            startActivity(new Intent(IndexActivity.this, GuideActivity.class));
                            editor.putBoolean(AppConst.SP_IS_FIRST_APP,false).commit();
                        }else {
                            startActivity(new Intent(IndexActivity.this, SplashActivity.class));

                        }
                    finish();
                }

            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        //引导页延迟显示
        mHandler.sendEmptyMessageDelayed(1,2000);
    }

}

