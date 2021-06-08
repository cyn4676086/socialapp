package com.smile.wechat.adapter;



import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.smile.wechat.fragment.BaseFragment;

import java.util.List;

/**
 * @创建者 CYN
 * @描述 主界面中ViewPager的适配器
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    private List<BaseFragment> mFragments;

    public MainPagerAdapter(FragmentManager fm, List<BaseFragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
