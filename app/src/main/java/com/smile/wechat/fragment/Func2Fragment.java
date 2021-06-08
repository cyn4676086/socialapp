package com.smile.wechat.fragment;

import android.view.View;
import android.widget.LinearLayout;

import com.smile.wechat.R;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * @创建者 CYN
 * @描述 聊天界面功能页面2
 */
public class Func2Fragment extends BaseFragment {

    @BindView(R.id.llVoice)
    LinearLayout mLlVoice;

    @Override
    public View initView() {
        View view = View.inflate(getActivity(), R.layout.fragment_func_page2, null);
        ButterKnife.bind(this, view);
        return view;
    }
}
