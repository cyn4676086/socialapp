package com.smile.wechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.viewpager.widget.ViewPager;

import com.smile.wechat.R;
import com.smile.wechat.nimsdk.NimAccountSDK;
import com.smile.wechat.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @创建者 CYN
 * @描述 欢迎界面
 */
public class SplashActivity extends BaseActivity {

    @BindView(R.id.btnLogin)
    Button mBtnLogin;
    @BindView(R.id.btnRegister)
    Button mBtnRegister;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //给btn1绑定监听事件
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashActivity.this,LoginActivity.class));
            }});
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashActivity.this,RegisterActivity.class));
            }});

    }
    @Override
    public void init() {
        if (canAutoLogin()) {
            //登录到主界面
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
    }

    /**
     * 判断是否可以自动登录
     *
     * @return
     */
    public boolean canAutoLogin() {
        String account = NimAccountSDK.getUserAccount();
        String token = NimAccountSDK.getUserAccount();
        return !TextUtils.isEmpty(account) && !TextUtils.isEmpty(token);
    }

}
