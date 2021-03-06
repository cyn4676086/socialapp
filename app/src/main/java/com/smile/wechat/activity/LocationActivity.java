package com.smile.wechat.activity;

import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.recyclerview.LQRRecyclerView;
import com.smile.wechat.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * @创建者 CYN
 * @描述 位置
 */
public class LocationActivity extends BaseActivity {

    private List<String> mData = new ArrayList<>();

    @BindView(R.id.cvLocation)
    LQRRecyclerView mCvLocation;
    private LQRAdapterForRecyclerView<String> mAdapter;

    @Override
    public void initView() {
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {
        for (int i = 0; i < 100; i++) {
            mData.add("item " + i);
        }
        setAdapter();
    }

    private void setAdapter() {
        mAdapter = new LQRAdapterForRecyclerView<String>(this, R.layout.item_contact_cv, mData) {
            @Override
            public void convert(LQRViewHolderForRecyclerView helper, String item, int position) {
                helper.setText(R.id.tvName, item);
            }
        };
        mCvLocation.setAdapter(mAdapter);
    }
}
