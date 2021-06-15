package com.smile.wechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.smile.wechat.R;
import com.smile.wechat.model.UserCache;
import com.smile.wechat.nimsdk.NimAccountSDK;
import com.smile.wechat.nimsdk.NimClientHandle;
import com.smile.wechat.nimsdk.OnRegisterListener;
import com.smile.wechat.nimsdk.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 更新密码
 */

public class ChangePassActivity extends BaseActivity {

    @BindView(R.id.et_account)
    EditText mEtAccount;
    @BindView(R.id.et_pass_word)
    EditText mEtPass;
    @BindView(R.id.et_confirm_pass)
    EditText mEtConfirmPass;
    @BindView(R.id.et_now_pass_word)
    EditText mEtNowPass;
    Toolbar mToolbar;

    private AbortableFuture<LoginInfo> mLoginFuture;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        ButterKnife.bind(this);


    }


    @OnClick(R.id.btn_update)
    public void register(){
        String account = mEtAccount.getText().toString().trim();
        String nowPass = mEtNowPass.getText().toString().trim();
        final String pass = mEtPass.getText().toString().trim();
        String confirmPass = mEtConfirmPass.getText().toString().trim();
        if (TextUtils.isEmpty(nowPass)){
            ToastUtils.showMessage(this,"请填写当前密码");
            return;
        }
        if (TextUtils.isEmpty(confirmPass) || !confirmPass.equals(pass)){
            ToastUtils.showMessage(this,"确认密码为空或与密码不符");
            return;
        }
        //验证当前账号密码
       
        if(NimAccountSDK.getUserAccount().equals(account)&&NimAccountSDK.getUserToken().equals(nowPass)){
            //修改密码
            NimClientHandle.getInstance().updateToken(account,pass, new OnRegisterListener() {
                @Override
                public void onSuccess() {
                    ToastUtils.showMessage(com.smile.wechat.activity.ChangePassActivity.this,"更新成功");

                    NIMClient.getService(AuthService.class).logout();
                    Intent intent = new Intent(com.smile.wechat.activity.ChangePassActivity.this,LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @Override
                public void onFailed(String message) {
                    ToastUtils.showMessage(com.smile.wechat.activity.ChangePassActivity.this,"改密失败:" + message);
                }
            });
        }else {
            ToastUtils.showMessage(this,"当前密码填写错误！");
        }




    }



}
