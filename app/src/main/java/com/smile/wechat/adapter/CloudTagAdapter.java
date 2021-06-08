package com.smile.wechat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moxun.tagcloudlib.view.TagsAdapter;
import com.smile.wechat.R;
import com.smile.wechat.helper.GlideHelper;
import com.smile.wechat.model.StarModel;

import java.util.List;

public class CloudTagAdapter extends TagsAdapter {

    private final Context mContext;
    private final List<StarModel> mList;
    private final LayoutInflater inflater;



    public CloudTagAdapter(Context mContext, List<StarModel> mList) {
        this.mContext = mContext;
        this.mList = mList;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View getView(Context context, int position, ViewGroup parent) {
        //初始化VIEW
        View view =inflater.inflate(R.layout.layout_star_view_item,null);
        ImageView iv_star_icon=view.findViewById(R.id.iv_star_icon);
        TextView tv_star_name=view.findViewById(R.id.tv_star_name);

        StarModel model=mList.get(position);
        GlideHelper.loadUrl(mContext,model.getPhotoUrl(),iv_star_icon);
        tv_star_name.setText(model.getNickName());
        //返回view
        return view;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getPopularity(int position) {
        return 7;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {

    }
}

