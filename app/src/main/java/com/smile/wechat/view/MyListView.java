package com.smile.wechat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * @创建者 CYN
 * @描述 解决ScrollView下嵌套ListView显示不全的问题
 */
public class MyListView extends ListView {
    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListView(Context context) {
        super(context);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}