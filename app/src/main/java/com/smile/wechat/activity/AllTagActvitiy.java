package com.smile.wechat.activity;

import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.smile.wechat.R;
import com.smile.wechat.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @创建者 CYN
 * @描述 所有标签界面
 */
public class AllTagActvitiy extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    public void initView() {
        setContentView(R.layout.activity_all_tag);
        ButterKnife.bind(this);
        initToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_one_text, menu);
        menu.getItem(0).setTitle("新建");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.itemOne:
                UIUtils.showToast("新建");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("所有标签");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
    }
}
