package com.smile.wechat.activity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.view.MenuItem;

import com.smile.wechat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @创建者 CYN
 * @描述 账号与安全
 */
public class AccountAndSafeSetActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    public void initView() {
        setContentView(R.layout.activity_account_and_safe_set);
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
        getSupportActionBar().setTitle("账号与安全");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
    }

    @OnClick(R.id.change)
    public void onclick(){
        Intent intent = new Intent(this, ChangePassActivity.class);
        startActivity(intent);
    }

}