package com.smile.wechat.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lqr.imagepicker.ui.ImageGridActivity;
import com.smile.wechat.R;
import com.smile.wechat.activity.MeetingActivity;
import com.smile.wechat.activity.RedPacketActivity;
import com.smile.wechat.activity.SessionActivity;
import com.smile.wechat.activity.TransferActivity;
import com.smile.wechat.view.CustomDialog;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

import static com.smile.wechat.R.id.tvOne;
import static com.smile.wechat.R.id.tvTwo;
import static com.smile.wechat.activity.SessionActivity.IMAGE_PICKER;

/**
 * @创建者 CYN
 * @描述 聊天界面功能页面1
 */
public class Func1Fragment extends BaseFragment {

    private View mContentView;
    private CustomDialog mDialog;
    private TextView mTvOne;
    private TextView mTvTwo;

    @BindView(R.id.llPic)
    LinearLayout mLlPic;
    @BindView(R.id.llRecord)
    LinearLayout mLlRecord;
    @BindView(R.id.llRedPacket)
    LinearLayout mLlRedPacket;
    @BindView(R.id.llTransfer)
    LinearLayout mLlTransfer;

    @BindView(R.id.llCollection)
    LinearLayout mLlCollection;
    @BindView(R.id.llLocation)
    LinearLayout mLlLocation;
    @BindView(R.id.llVideo)
    LinearLayout mLlVideo;
    @BindView(R.id.llBusinessCard)
    LinearLayout mLlBusinessCard;

    Intent mIntent;

    @OnClick({R.id.llPic, R.id.llRecord, R.id.llRedPacket, R.id.llTransfer, R.id.llLocation, R.id.llVideo})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.llPic:
                mIntent = new Intent(getActivity(), ImageGridActivity.class);
                startActivityForResult(mIntent, IMAGE_PICKER);
                break;
            case R.id.llRecord:
                ((SessionActivity)getActivity()).showPlayVideo();
                break;
            case R.id.llRedPacket:
                mIntent = new Intent(getActivity(), RedPacketActivity.class);
                startActivity(mIntent);
                break;
            case R.id.llTransfer:
                mIntent = new Intent(getActivity(), TransferActivity.class);
                startActivity(mIntent);
                break;
            case R.id.llLocation:
                mContentView = View.inflate(getActivity(), R.layout.dialog_menu_two_session, null);
                mDialog = new CustomDialog(getActivity(), mContentView, R.style.dialog);
                mDialog.show();
                mTvOne = (TextView) mContentView.findViewById(tvOne);
                mTvTwo = (TextView) mContentView.findViewById(tvTwo);
                mTvOne.setText("发送位置");
                mTvTwo.setText("共享实时位置");
                mTvOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });
                mTvTwo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });

                break;
            case R.id.llVideo:
                mContentView = View.inflate(getActivity(), R.layout.dialog_menu_two_session, null);
                mDialog = new CustomDialog(getActivity(), mContentView, R.style.dialog);
                mDialog.show();
                mTvOne = (TextView) mContentView.findViewById(tvOne);
                //mTvTwo = (TextView) mContentView.findViewById(tvTwo);
                mTvOne.setText("视频聊天");
                //mTvTwo.setText("语音聊天");
                mTvOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MeetingActivity.startActivity(getActivity(),"2021");
                        mDialog.dismiss();
                    }
                });
                break;

        }
    }

    @Override
    public View initView() {
        View view = View.inflate(getActivity(), R.layout.fragment_func_page1, null);
        ButterKnife.bind(this, view);
        return view;
    }

}
