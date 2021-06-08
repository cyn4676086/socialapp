package com.smile.wechat.activity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.smile.wechat.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @创建者 CYN
 * @描述 关于Smlie
 */
public class AboutActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    public void initView() {
        setContentView(R.layout.activity_about);
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
        getSupportActionBar().setTitle("关于Smlie");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
    }
}
