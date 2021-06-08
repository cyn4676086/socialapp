package com.smile.wechat.factory;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ListView;

import com.smile.wechat.utils.UIUtils;


/**
 * @创建者 CYN
 * @描述 ListView工厂（已经配置好了样式）
 */
public class ListViewFactory {

    public static ListView createListView() {
        ListView listView = new ListView(UIUtils.getContext());

        // 简单的设置
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setFastScrollEnabled(true);

        //去掉listview的item点击时的蓝色背景
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        return listView;
    }
}
