package com.smile.wechat.activity;

import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.smile.wechat.R;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * @创建者 CYN
 * @描述 隐私
 */
public class PrivacySetActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    public void initView() {
        setContentView(R.layout.activity_privacy_set);
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
        getSupportActionBar().setTitle("隐私");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
    }
}
