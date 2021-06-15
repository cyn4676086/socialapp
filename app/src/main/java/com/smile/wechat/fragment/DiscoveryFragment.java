package com.smile.wechat.fragment;
import android.content.Intent;
import android.view.View;

import com.smile.wechat.AppConst;
import com.smile.wechat.R;
import com.smile.wechat.activity.MainActivity;
import com.smile.wechat.activity.NearbyPerpleActivity;
import com.smile.wechat.activity.ScanActivity;
import com.smile.wechat.activity.SettingActivity;
import com.smile.wechat.activity.SquareActivity;
import com.smile.wechat.activity.WebViewActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @创建者 CSDN_LQR
 * @描述 发现
 */
public class DiscoveryFragment extends BaseFragment {

    private Intent mIntent;

    @OnClick({R.id.oivScan, R.id.oivNearby, R.id.oivShop, R.id.oivGame})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.oivScan:
                startActivity(new Intent(getActivity(), SquareActivity.class));
                break;
            case R.id.oivNearby:
                startActivity(new Intent(getActivity(), NearbyPerpleActivity.class));
                break;
            case R.id.oivShop:
                mIntent = new Intent(getActivity(), WebViewActivity.class);
                mIntent.putExtra("url", AppConst.Url.SHOP);
                mIntent.putExtra("title", "京东购物");
                startActivity(mIntent);
                break;
            case R.id.oivGame:
                mIntent = new Intent(getActivity(), WebViewActivity.class);
                mIntent.putExtra("url", AppConst.Url.GAME);
                mIntent.putExtra("title", "游戏");
                startActivity(mIntent);
                break;
        }
    }

    @Override
    public View initView() {
        View view = View.inflate(getActivity(), R.layout.fragment_discovery, null);
        ButterKnife.bind(this, view);
        return view;
    }
}