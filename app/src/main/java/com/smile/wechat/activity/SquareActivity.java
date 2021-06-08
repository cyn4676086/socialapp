package com.smile.wechat.activity;


import java.util.ArrayList;
import java.util.List;
import androidx.viewpager.widget.ViewPager;
import com.smile.wechat.R;
import com.smile.wechat.adapter.MainPagerAdapter;
import com.smile.wechat.fragment.BaseFragment;
import com.smile.wechat.fragment.SquareFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SquareActivity extends BaseActivity{

    private SquareFragment mSquareFragment;

    private List<BaseFragment> mFragments;


    @BindView(R.id.vpContent)
    ViewPager mVpContent;

    @Override
    public void initView() {
        setContentView(R.layout.activity_square);
        ButterKnife.bind(this);

        //设置ViewPager的最大缓存页面
        mVpContent.setOffscreenPageLimit(3);

    }

    @Override
    public void initData() {
        //创建Fragment
        mFragments = new ArrayList<>();
        mSquareFragment=new SquareFragment();

        mFragments.add(mSquareFragment);

        //设置中间内容vp适配器
        mVpContent.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), mFragments));
        mVpContent.setCurrentItem(0);

    }
}
