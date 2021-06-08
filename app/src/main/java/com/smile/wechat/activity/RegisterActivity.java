package com.smile.wechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.smile.wechat.R;
import com.smile.wechat.nimsdk.NimClientHandle;
import com.smile.wechat.nimsdk.OnRegisterListener;
import com.smile.wechat.nimsdk.utils.ToastUtils;

/**
 * 注册账户
 */
public class RegisterActivity extends BaseActivity {



    private EditText userid;
    private EditText pwd;
    private EditText username;
    private EditText cf_pwd;
    private Button to_login;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViews();

    }

    private void findViews(){
        userid = findViewById(R.id.et_rg_userid);
        pwd=findViewById(R.id.et_rg_pwd);
        username=findViewById(R.id.et_rg_username);
        cf_pwd=findViewById(R.id.et_rg_confirm_pwd);
        loginButton=findViewById(R.id.btn_register);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        to_login=findViewById(R.id.btn_to_login);
        to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(com.smile.wechat.activity.RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    public void register(){

        String account = userid.getText().toString();
        String name = username.getText().toString();
        String pass = pwd.getText().toString();
        String confirmPass = cf_pwd.getText().toString();
        if(account.isEmpty() || name.isEmpty() || pass.isEmpty()){
            Toast.makeText(com.smile.wechat.activity.RegisterActivity.this,"请将信息填写完整", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!(account.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{2,16}$"))){
            Toast.makeText(com.smile.wechat.activity.RegisterActivity.this,"ID必须是字母+数字组合，2到16字符长度", Toast.LENGTH_SHORT).show();
            return;
        }
        if (confirmPass.isEmpty() || !confirmPass.equals(pass)){
            Toast.makeText(com.smile.wechat.activity.RegisterActivity.this,"请确保密码一致！", Toast.LENGTH_LONG).show();

            return;
        }

        NimClientHandle.getInstance().register(account,pass, name, new OnRegisterListener() {
            @Override
            public void onSuccess() {
                ToastUtils.showMessage(com.smile.wechat.activity.RegisterActivity.this,"注册成功");
                finish();
            }

            @Override
            public void onFailed(String message) {
                ToastUtils.showMessage(com.smile.wechat.activity.RegisterActivity.this,"注册失败:" + message);
            }
        });

    }

}



