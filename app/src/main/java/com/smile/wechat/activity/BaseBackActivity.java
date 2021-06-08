package com.smile.wechat.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;


/**
 * FileName: BaseBackActivity
 * Profile: 有返回键的基类,必须有actionbar，否则闪退
 */
public class BaseBackActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //显示返回键
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //清除阴影

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
