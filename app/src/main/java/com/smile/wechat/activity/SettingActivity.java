package com.smile.wechat.activity;

import android.content.Intent;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.smile.wechat.App;
import com.smile.wechat.R;
import com.smile.wechat.nimsdk.NimAccountSDK;
import com.smile.wechat.view.CustomDialog;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * @创建者 CYN
 * @描述 设置界面
 */
public class SettingActivity extends BaseActivity {

    Intent intent;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private View mExitDialogView;
    private CustomDialog mDialog;

    @OnClick({R.id.oivNewMsgNotifySet, R.id.oivCheatSet, R.id.oivPrivacySet, R.id.oivCommon, R.id.oivAccountAndSafeSet, R.id.oivAbout, R.id.oivExit})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.oivNewMsgNotifySet:
                intent = new Intent(this, NewMsgNotifySetActivity.class);
                startActivity(intent);
                break;
            case R.id.oivCheatSet:
                intent = new Intent(this, CheatSetActivity.class);
                startActivity(intent);
                break;
            case R.id.oivPrivacySet:
                intent = new Intent(this, PrivacySetActivity.class);
                startActivity(intent);
                break;
            case R.id.oivCommon:
                intent = new Intent(this, CommonSetActivity.class);
                startActivity(intent);
                break;
            case R.id.oivAccountAndSafeSet:
                intent = new Intent(this, AccountAndSafeSetActivity.class);
                startActivity(intent);
                break;
            case R.id.oivAbout:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.oivExit:
                if (mExitDialogView == null) {
                    mExitDialogView = View.inflate(this, R.layout.dialog_exit, null);
                    mDialog = new CustomDialog(this, mExitDialogView, R.style.dialog);
                    mDialog.show();

                    mExitDialogView.findViewById(R.id.tvExitAccount).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //学校账号
                            NimAccountSDK.logout();
                            NimAccountSDK.saveUserAccount("");
                            intent = new Intent(SettingActivity.this, SplashActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            mDialog.dismiss();
                        }
                    });

                    mExitDialogView.findViewById(R.id.tvExitApp).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //退出app
                            App.exit();
                            mDialog.dismiss();
                        }
                    });

                } else {
                    mDialog.show();
                }
                break;
        }
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        initToolbar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
    }
}
