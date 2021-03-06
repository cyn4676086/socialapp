package com.smile.wechat.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lqr.emoji.EmoticonPickerView;
import com.lqr.emoji.EmotionKeyboard;
import com.lqr.emoji.IEmoticonSelectedListener;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;
import com.lqr.imagepicker.ui.ImagePreviewActivity;
import com.lqr.recyclerview.LQRRecyclerView;
import com.lqr.videorecordview.LQRVideoRecordView;
import com.smile.wechat.R;
import com.smile.wechat.adapter.FuncPagerAdapter;
import com.smile.wechat.adapter.SessionAdapter;
import com.smile.wechat.factory.ThreadPoolFactory;
import com.smile.wechat.fragment.BaseFragment;
import com.smile.wechat.fragment.Func1Fragment;
import com.smile.wechat.fragment.Func2Fragment;
import com.smile.wechat.model.Contact;
import com.smile.wechat.nimsdk.NimHistorySDK;
import com.smile.wechat.nimsdk.NimMessageSDK;
import com.smile.wechat.nimsdk.NimTeamSDK;
import com.smile.wechat.nimsdk.custom.StickerAttachment;
import com.smile.wechat.nimsdk.helper.SendImageHelper;
import com.smile.wechat.utils.KeyBoardUtils;
import com.smile.wechat.utils.LogUtils;
import com.smile.wechat.utils.UIUtils;
import com.smile.wechat.view.DotView;
import com.smile.wechat.view.LQRRecordProgress;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;
import butterknife.OnTouch;
import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.BGARefreshViewHolder;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

/**
 * @????????? CYN
 * @?????? ????????????
 * <p>
 * ???????????????????????????????????????????????????????????? ???????????????????????????????????????????????????????????????
 */
public class SessionActivity extends BaseActivity implements IEmoticonSelectedListener, BGARefreshLayout.BGARefreshLayoutDelegate, IAudioRecordCallback, LQRVideoRecordView.OnRecordStausChangeListener {

    public static final int IMAGE_PICKER = 100;

    public static final String SESSION_ACCOUNT = "account";
    public static final String SESSION_TYPE = "type";

    //??????????????????
    public String mSessionId;//?????????????????????id???????????????id
    private Contact mContact;
    private Team mTeam;

    public SessionTypeEnum mSessionType = SessionTypeEnum.P2P;
    //?????????????????????
    private Observer<IMMessage> mMsgStatusObserver;
    private Observer<List<IMMessage>> mIncomingMessageObserver;
    private Observer<AttachmentProgress> mAttachmentProgressObserver;
    private List<IMMessage> mMessages = new ArrayList<>();
    private SessionAdapter mAdapter;

    private Runnable mCvMessageScrollToBottomTask = new Runnable() {
        @Override
        public void run() {
            mCvMessage.moveToPosition(mMessages.size() - 1);
        }
    };
    //??????????????????
    private IMMessage mAnchor;
    private QueryDirectionEnum mDirection = QueryDirectionEnum.QUERY_OLD;//?????????????????????
    private static final int LOAD_MESSAGE_COUNT = 20;
    private boolean mFirstLoad = true;

    private boolean mRemote = false;
    //????????????
    private FuncPagerAdapter mBottomFucAdapter;
    private List<BaseFragment> mFragments;

    private EmotionKeyboard mEmotionKeyboard;
    //??????
    private AudioRecorder mAudioRecorderHelper;
    private boolean mStartRecord;
    private boolean mCanclled;

    private boolean mTouched;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.refreshLayout)
    BGARefreshLayout mRefreshLayout;

    @BindView(R.id.cvMessage)
    LQRRecyclerView mCvMessage;

    @BindView(R.id.llButtomFunc)
    LinearLayout mLlButtomFunc;
    @BindView(R.id.ivAudio)
    ImageView mIvAudio;
    @BindView(R.id.etContent)
    EditText mEtContent;
    @BindView(R.id.btnAudio)
    Button mBtnAudio;
    @BindView(R.id.ivEmo)
    ImageView mIvEmo;
    @BindView(R.id.ivAdd)
    ImageView mIvAdd;

    @BindView(R.id.btnSend)
    Button mBtnSend;
    @BindView(R.id.flBottom)
    FrameLayout mFlBottom;
    @BindView(R.id.epv)
    EmoticonPickerView mEpv;
    @BindView(R.id.vpFunc)
    ViewPager mVpFunc;

    @BindView(R.id.dv)
    DotView mDv;
    @BindView(R.id.flPlayAudio)
    FrameLayout mFlPlayAudio;
    @BindView(R.id.cTimer)
    Chronometer mCTimer;

    @BindView(R.id.tvTimerTip)
    TextView mTvTimerTip;
    @BindView(R.id.llPlayVideo)
    LinearLayout mLlPlayVideo;
    @BindView(R.id.vrvVideo)
    LQRVideoRecordView mVrvVideo;
    @BindView(R.id.tvTipOne)
    TextView mTvTipOne;
    @BindView(R.id.tvTipTwo)
    TextView mTvTipTwo;
    @BindView(R.id.rp)
    LQRRecordProgress mRp;
    @BindView(R.id.btnVideo)
    Button mBtnVideo;
    private Observer<List<TeamMember>> memberRemoveObserver;
    private Observer<List<TeamMember>> memberUpdateObserver;

    @OnTouch(R.id.cvMessage)
    public boolean cvTouch() {
        if (mEtContent.hasFocus()) {
            closeKeyBoardAndLoseFocus();
            return true;
        } else if (mFlBottom.getVisibility() == View.VISIBLE) {
            mFlBottom.setVisibility(View.GONE);
            closeKeyBoardAndLoseFocus();
            return true;
        }
        return false;
    }

    @OnClick({R.id.ivAudio, R.id.btnSend})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.ivAudio:
                toggleAudioButtonVisibility();
                break;
            case R.id.btnSend:
                sendTextMsg();
                break;
        }
    }

    @Override
    public void init() {
        Intent intent = getIntent();
        SessionTypeEnum sessionType = (SessionTypeEnum) intent.getSerializableExtra(SESSION_TYPE);
        if (sessionType != null) {
            mSessionType = sessionType;
        }

        mSessionId = intent.getStringExtra(SESSION_ACCOUNT);
        if (TextUtils.isEmpty(mSessionId)) {
            interrupt();
            return;
        }

        registerAllObserver();
        requestPermission();
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_session);
        ButterKnife.bind(this);
        initToolbar();
        initEmotionPickerView();
        initEmotionKeyboard();
        initRefreshLayout();
        initBottomFunc();

        //??????RecyclerView?????????????????????
//        ((DefaultItemAnimator) mCvMessage.getItemAnimator()).setSupportsChangeAnimations(false);

        closeKeyBoardAndLoseFocus();
    }

    @Override
    public void initData() {
        //??????????????????(????????????)
        mMessages.clear();
        setAdapter();
        loadHistoryMsgFromLocal();

        if (mSessionType == SessionTypeEnum.P2P) {
            mContact = new Contact(mSessionId);
            //???????????????????????????????????????/?????????
            getSupportActionBar().setTitle(TextUtils.isEmpty(mContact.getAlias()) ? mContact.getName() : mContact.getAlias());
        } else {
            //??????????????????????????????
            ThreadPoolFactory.getNormalPool().execute(new Runnable() {
                @Override
                public void run() {
                    mTeam = NimTeamSDK.queryTeamBlock(mSessionId);
                    UIUtils.postTaskSafely(new Runnable() {
                        @Override
                        public void run() {
                            getSupportActionBar().setTitle(TextUtils.isEmpty(mTeam.getName()) ? "??????(" + mTeam.getMemberCount() + ")" : mTeam.getName());
                        }
                    });
                }
            });

        }
    }


    @Override
    public void initListener() {
        //?????????????????????????????????????????????????????????????????????????????????
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(mEtContent.getText().toString())) {
                    mIvAdd.setVisibility(View.VISIBLE);
                    mBtnSend.setVisibility(View.GONE);
                } else {
                    mIvAdd.setVisibility(View.GONE);
                    mBtnSend.setVisibility(View.VISIBLE);
                }
            }
        });

        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        mEtContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cvScrollToBottom();
                }
            }
        });

        //??????ViewPager??????????????????????????????????????????
        mVpFunc.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //?????????????????????
                mDv.changeCurrentPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //???????????????????????????
//        "?????? ??????"
//        "?????? ??????"
//        "???????????????????????????"
        mBtnAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTouched = true;
                        initAudioRecord();
                        onStartAudioRecord();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mTouched = false;
                        cancelAudioRecord(isCancelled(v, event));
                        break;
                    case MotionEvent.ACTION_UP:
                        mTouched = false;
                        hidePlayAudio();
                        onEndAudioRecord(isCancelled(v, event));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        mTouched = false;
                        hidePlayAudio();
                        onEndAudioRecord(isCancelled(v, event));
                        break;
                }
                return false;
            }
        });

        //???????????????????????????
        mBtnVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mRp.start();
                        mRp.setProgressColor(Color.parseColor("#1AAD19"));
                        mTvTipOne.setVisibility(View.VISIBLE);
                        mTvTipTwo.setVisibility(View.GONE);
                        //????????????
                        mVrvVideo.record(SessionActivity.this);
                        break;
                    case MotionEvent.ACTION_UP:
                        mRp.stop();
                        mTvTipOne.setVisibility(View.GONE);
                        mTvTipTwo.setVisibility(View.GONE);
                        //????????????
                        if (mVrvVideo.getTimeCount() > 3) {
                            if (!isCancelled(v, event)) {
                                onRecrodFinish();
                            } else {
                                if (mVrvVideo.getVecordFile() != null)
                                    mVrvVideo.getVecordFile().delete();
                            }
                        } else {
                            if (!isCancelled(v, event)) {
                                Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                            } else {
                                if (mVrvVideo.getVecordFile() != null)
                                    mVrvVideo.getVecordFile().delete();
                            }
                        }
                        resetVideoRecord();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isCancelled(v, event)) {
                            mTvTipOne.setVisibility(View.GONE);
                            mTvTipTwo.setVisibility(View.VISIBLE);
                            mRp.setProgressColor(Color.parseColor("#FF1493"));
                        } else {
                            mTvTipOne.setVisibility(View.VISIBLE);
                            mTvTipTwo.setVisibility(View.GONE);
                            mRp.setProgressColor(Color.parseColor("#1AAD19"));
                        }
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        if (mSessionType == SessionTypeEnum.Team) {
            mTeam = NimTeamSDK.queryTeamBlock(mSessionId);
            getSupportActionBar().setTitle(TextUtils.isEmpty(mTeam.getName()) ? "??????(" + mTeam.getMemberCount() + ")" : mTeam.getName());
        }
        setAdapter();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.itemFriendInfo:
                Intent intent;
                if (mSessionType == SessionTypeEnum.P2P) {
                    intent = new Intent(SessionActivity.this, UserInfoActivity.class);
                    intent.putExtra(UserInfoActivity.USER_INFO_ACCOUNT, mSessionId);
                    startActivity(intent);
                } else {
                    intent = new Intent(SessionActivity.this, TeamCheatInfoActivity.class);
                    intent.putExtra(TeamCheatInfoActivity.GROUP_CHEAT_INFO_TEAMID, mSessionId);
                    startActivityForResult(intent, 100);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {//??????????????????
            if (data != null) {
                //??????????????????
                boolean isOrig = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);

                for (ImageItem imageItem : images) {
                    new SendImageHelper.SendImageTask(SessionActivity.this, isOrig, imageItem, new SendImageHelper.Callback() {
                        @Override
                        public void sendImage(File file, boolean isOrig) {
                            sendImagesMsg(file);
                        }
                    }).execute();
                }
            }
        } else if (resultCode == TeamCheatInfoActivity.RESP_QUIT_TEAM || resultCode == TeamCheatInfoActivity.RESP_CHEAT_SINGLE) {
            finish();
        } else if (resultCode == TeamCheatInfoActivity.RESP_CLEAR_CHATTING_RECORD_HISTORY) {
            mAdapter.clearData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //?????????????????????????????????????????????
        unRegisterAllObserver();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
    }

    private void initRefreshLayout() {
        // ???BGARefreshLayout ????????????
        mRefreshLayout.setDelegate(this);
        // ????????????????????????????????????????????????     ??????1?????????????????????????????????2???????????????????????????????????????
        BGARefreshViewHolder refreshViewHolder = new BGANormalRefreshViewHolder(this, false);
        // ????????????????????????????????????????????????
        mRefreshLayout.setRefreshViewHolder(refreshViewHolder);
    }

    /**
     * ????????????????????????
     */
    private void initBottomFunc() {
        //???????????????
        mFragments = new ArrayList<>();
        Func1Fragment func1Fragment1 = new Func1Fragment();
        Func2Fragment func1Fragment2 = new Func2Fragment();
        mFragments.add(func1Fragment1);
        mFragments.add(func1Fragment2);
        mBottomFucAdapter = new FuncPagerAdapter(getSupportFragmentManager(), mFragments);
        mVpFunc.setAdapter(mBottomFucAdapter);

        //???????????????????????????????????????????????????
        mDv.initData(mFragments.size(), 0);
    }

    public void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new SessionAdapter(this, mMessages);
            mCvMessage.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void registerAllObserver() {
        observeMsgStatus();
        observeReceiveMessage();
        observerAttachmentProgressObserver();
        if (mSessionType == SessionTypeEnum.Team) {
            observeMemberRemove();
            observeMemberUpdate();
        }
    }

    private void unRegisterAllObserver() {
        NimMessageSDK.observeMsgStatus(mMsgStatusObserver, false);
        NimMessageSDK.observeReceiveMessage(mIncomingMessageObserver, false);
        NimMessageSDK.observeAttachProgress(mAttachmentProgressObserver, false);
        if (mSessionType == SessionTypeEnum.Team) {
            NimTeamSDK.observeMemberRemove(memberRemoveObserver, false);
            NimTeamSDK.observeMemberUpdate(memberUpdateObserver, false);
        }
    }

    /**
     * ????????????????????????
     */
    private void observeMsgStatus() {
        mMsgStatusObserver = new Observer<IMMessage>() {
            @Override
            public void onEvent(IMMessage imMessage) {
                if (NimMessageSDK.isCurrentSessionMessage(imMessage, mSessionId, mSessionType)) {
                    onMessageStatusChange(imMessage);
                }
            }
        };
        NimMessageSDK.observeMsgStatus(mMsgStatusObserver, true);
    }

    /**
     * ????????????????????????
     */
    private void observeReceiveMessage() {
        mIncomingMessageObserver = new Observer<List<IMMessage>>() {
            @Override
            public void onEvent(List<IMMessage> imMessages) {
                if (imMessages == null || imMessages.isEmpty()) {
                    return;
                }

                //??????????????????????????????
                List<IMMessage> currentMsgList = new ArrayList<>();
                for (IMMessage msg : imMessages) {
                    if (NimMessageSDK.isCurrentSessionMessage(msg, mSessionId, mSessionType)) {
                        currentMsgList.add(msg);
                    }
                }

                //??????????????????????????????????????????????????????
                int theLastOnePosition = mAdapter.getData().size() - 1;
                mAdapter.addMoreData(currentMsgList);

                //???????????????????????????????????????????????????????????????
                int lastVisibleItemPosition = ((GridLayoutManager) mCvMessage.getLayoutManager()).findLastVisibleItemPosition();
                if (lastVisibleItemPosition == theLastOnePosition)
                    cvScrollToBottom();

            }
        };
        NimMessageSDK.observeReceiveMessage(mIncomingMessageObserver, true);
    }

    /**
     * ??????????????????/?????????????????????
     */
    private void observerAttachmentProgressObserver() {
        mAttachmentProgressObserver = new Observer<AttachmentProgress>() {
            @Override
            public void onEvent(AttachmentProgress progress) {
                onAttachmentProgressChange(progress);
            }
        };
        NimMessageSDK.observeAttachProgress(mAttachmentProgressObserver, true);
    }

    private void observeMemberUpdate() {
        memberUpdateObserver = new Observer<List<TeamMember>>() {
            @Override
            public void onEvent(List<TeamMember> teamMembers) {
                onResume();
            }
        };
        NimTeamSDK.observeMemberUpdate(memberUpdateObserver, true);
    }

    private void observeMemberRemove() {
        memberRemoveObserver = new Observer<List<TeamMember>>() {
            @Override
            public void onEvent(List<TeamMember> teamMembers) {
                onResume();
            }


        };
        NimTeamSDK.observeMemberRemove(memberRemoveObserver, true);
    }

    private void onMessageStatusChange(IMMessage message) {
        int index = getItemIndex(message.getUuid());
        if (index >= 0 && index < mMessages.size()) {
            IMMessage msg = mMessages.get(index);
            msg.setStatus(message.getStatus());
            msg.setAttachStatus(message.getAttachStatus());
            mAdapter.notifyItemChanged(index);
        }
    }

    private void onAttachmentProgressChange(AttachmentProgress progress) {
        int index = getItemIndex(progress.getUuid());
        if (index >= 0 && index < mMessages.size()) {
            IMMessage item = mMessages.get(index);
            LogUtils.sf("Transferred = " + progress.getTransferred());
            LogUtils.sf("Total = " + progress.getTotal());
            float value = (float) progress.getTransferred() / (float) progress.getTotal();
            mAdapter.putProgress(item, value * 100);
            mAdapter.notifyItemChanged(index);
        }
    }

    private int getItemIndex(String uuid) {
        for (int i = 0; i < mMessages.size(); i++) {
            IMMessage message = mMessages.get(i);
            if (TextUtils.equals(message.getUuid(), uuid)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * ????????????
     */
    private IMMessage getAnchor() {
        if (mMessages.size() == 0) {
            return mAnchor == null ? MessageBuilder.createEmptyMessage(mSessionId, mSessionType, 0) : mAnchor;
        } else {
            int index = (mDirection == QueryDirectionEnum.QUERY_NEW ? mMessages.size() - 1 : 0);
            return mMessages.get(index);
        }
    }

    /**
     * ???????????????????????????
     */
    private void loadHistoryMsgFromLocal() {
        LogUtils.sf("???????????????????????????");
        mDirection = QueryDirectionEnum.QUERY_OLD;
        mRemote = false;
        NimHistorySDK.queryMessageListEx(getAnchor(), mDirection, LOAD_MESSAGE_COUNT, true).setCallback(loadFromRemoteCallback);
    }

    /**
     * ???????????????????????????????????????
     */
//    private void loadNewMsgFromServer() {
//        LogUtils.sf("???????????????????????????????????????");
//        mDirection = QueryDirectionEnum.QUERY_NEW;
//        mRemote = true;
//        NimHistorySDK.pullMessageHistoryEx(getAnchor(), new DateTime(2017,1,5,23,59,59).getMillis(), LOAD_MESSAGE_COUNT, mDirection, true).setCallback(loadFromRemoteCallback);
//    }

    /**
     * ????????????????????????????????????
     */
    private void loadHistoryMsgFromRemote() {
        LogUtils.sf("????????????????????????????????????");
        mDirection = QueryDirectionEnum.QUERY_OLD;
        mRemote = true;
        NimHistorySDK.pullMessageHistory(getAnchor(), LOAD_MESSAGE_COUNT, true).setCallback(loadFromRemoteCallback);
    }

    private boolean mIsFirstLoadHistory = true;

    RequestCallback<List<IMMessage>> loadFromRemoteCallback = new RequestCallbackWrapper<List<IMMessage>>() {
        @Override
        public void onResult(int code, List<IMMessage> result, Throwable exception) {
            if (code != ResponseCode.RES_SUCCESS || exception != null) {
                return;
            }

            if (result == null)
                return;

            //???????????????????????????????????????????????????????????????????????????
            if (mIsFirstLoadHistory) {
                mIsFirstLoadHistory = false;
            }
            //????????????????????????????????????????????????????????????????????????
            else if (result.size() == 0 && !mRemote) {
                loadHistoryMsgFromRemote();
                return;
            }

            onMessageLoaded(result);
        }
    };


    /**
     * ????????????????????????
     *
     * @param messages
     */
    private void onMessageLoaded(List<IMMessage> messages) {
        if (mRemote) {
            Collections.reverse(messages);
        }

        if (mFirstLoad && mMessages.size() > 0) {
            // ?????????????????????????????????????????????????????????????????????
            for (IMMessage message : messages) {
                for (IMMessage item : mMessages) {
                    if (item.isTheSame(message)) {
                        mAdapter.removeItem(item);
                        break;
                    }
                }
            }
        }

        if (mFirstLoad && mAnchor != null) {
            mAdapter.addLastItem(mAnchor);
        }

        if (mDirection == QueryDirectionEnum.QUERY_NEW) {
            mAdapter.addMoreData(messages);
        } else {
            mAdapter.addNewData(messages);
        }

        if (mFirstLoad) {
            cvScrollToBottom();
        } else {
            if (messages.size() > 0) {
                mCvMessage.moveToPosition(messages.size() - 1);
            }
        }

        mRefreshLayout.endRefreshing();

        mFirstLoad = false;
    }

    /**
     * ??????????????????
     */
    public void sendTextMsg() {
        String content = mEtContent.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            IMMessage message = NimMessageSDK.createTextMessage(mSessionId, mSessionType, content);
            sendMsg(message);
            mEtContent.setText("");
        }
    }

    /**
     * ??????????????????
     *
     * @param stickerAttachment
     */
    private void sendStickerMsg(StickerAttachment stickerAttachment) {
        IMMessage stickerMessage = NimMessageSDK.createCustomMessage(mSessionId, mSessionType, "????????????", stickerAttachment);
        sendMsg(stickerMessage);
    }

    /**
     * ??????????????????
     */
    private void sendImagesMsg(File image) {
        IMMessage message = NimMessageSDK.createImageMessage(mSessionId, mSessionType, image.getAbsoluteFile(), image.getName());
        sendMsg(message);
    }

    /**
     * ??????????????????
     */
    private void sendAudioMsg(File audioFile, long audioLength) {
        IMMessage msg = NimMessageSDK.createAudioMessage(mSessionId, mSessionType, audioFile, audioLength);
        sendMsg(msg);
    }

    /**
     * ??????????????????
     */
    private void sendVidoMsg(File videoFile, String displayName) {
        IMMessage msg = NimMessageSDK.createVideoMessage(mSessionId, mSessionType, videoFile, displayName);
        sendMsg(msg);
    }


    /**
     * ???????????????????????????
     */
    private void sendMsg(IMMessage message) {
        NimMessageSDK.sendMessage(message);
        mAdapter.addLastItem(message);
        mAdapter.notifyDataSetChanged();
        cvScrollToBottom();
    }

    /**
     * ??????????????????????????????
     */
    private void openKeyBoardAndGetFocus() {
        mEtContent.requestFocus();
        KeyBoardUtils.openKeybord(mEtContent, this);
    }

    /**
     * ??????????????????????????????
     */
    private void closeKeyBoardAndLoseFocus() {
        mEtContent.clearFocus();
        KeyBoardUtils.closeKeybord(mEtContent, this);
        mFlBottom.setVisibility(View.GONE);
    }

    /**
     * ???????????????????????????
     */
    private void cvScrollToBottom() {
        UIUtils.postTaskDelay(mCvMessageScrollToBottomTask, 100);
    }

    /*================== ????????????????????? begin ==================*/

    /**
     * ???????????????????????????
     */
    private void initEmotionPickerView() {
        mEpv.setWithSticker(true);
        mEpv.show(this);
        mEpv.attachEditText(mEtContent);
    }

    /**
     * ????????????????????????
     */
    private void initEmotionKeyboard() {
        //1?????????EmotionKeyboard??????
        mEmotionKeyboard = EmotionKeyboard.with(this);
        //2????????????????????????
        mEmotionKeyboard.bindToEditText(mEtContent);
        //3???????????????????????????????????????????????????????????????RecyclerView?????????????????????????????????????????????????????????????????????????????????
        mEmotionKeyboard.bindToContent(mCvMessage);
        //4????????????????????????????????????????????????????????????????????????????????????FrameLayout??????????????????????????????FrameLayout???
        mEmotionKeyboard.setEmotionView(mFlBottom);
        //5????????????????????????????????????????????????Smlie??????2??????????????????????????????????????????????????????
        mEmotionKeyboard.bindToEmotionButton(mIvEmo, mIvAdd);
        //6????????????5?????????????????????EmotionButton??????????????????????????????view??????????????????????????????????????????????????????????????????????????????????????????EmotionKeyboard??????
        mEmotionKeyboard.setOnEmotionButtonOnClickListener(new EmotionKeyboard.OnEmotionButtonOnClickListener() {
            @Override
            public boolean onEmotionButtonOnClickListener(View view) {
                if (mBtnAudio.getVisibility() == View.VISIBLE) {
                    hideBtnAudio();
                }
                //????????????????????????
                if (mFlBottom.getVisibility() == View.VISIBLE) {
                    //???????????????????????????????????????ivAdd?????????????????????????????????????????????????????????
                    if (mEpv.getVisibility() == View.VISIBLE && view.getId() == R.id.ivAdd) {
                        mEpv.setVisibility(View.GONE);
                        mLlButtomFunc.setVisibility(View.VISIBLE);
                        return true;
                        //????????????????????????????????????ivEmo?????????????????????????????????????????????????????????
                    } else if (mLlButtomFunc.getVisibility() == View.VISIBLE && view.getId() == R.id.ivEmo) {
                        mEpv.setVisibility(View.VISIBLE);
                        mLlButtomFunc.setVisibility(View.GONE);
                        return true;
                    }
                } else {
                    //??????ivEmo?????????????????????
                    if (view.getId() == R.id.ivEmo) {
                        mEpv.setVisibility(View.VISIBLE);
                        mLlButtomFunc.setVisibility(View.GONE);
                        //??????ivAdd??????????????????
                    } else {
                        mEpv.setVisibility(View.GONE);
                        mLlButtomFunc.setVisibility(View.VISIBLE);
                    }
                }
                cvScrollToBottom();
                return false;
            }
        });
    }

    @Override
    public void onEmojiSelected(String s) {
    }

    @Override
    public void onStickerSelected(String catalog, String chartlet) {
        StickerAttachment stickerAttachment = new StickerAttachment(catalog, chartlet);
        sendStickerMsg(stickerAttachment);
    }

    /*================== ????????????????????? end ==================*/
    /*================== ?????????????????? begin ==================*/

    /**
     * ????????????????????????
     */
    public void toggleAudioButtonVisibility() {
        if (mBtnAudio.getVisibility() == View.VISIBLE) {
            hideBtnAudio();
        } else {
            showBtnAudio();
        }
        //????????????
        mIvAudio.setImageResource(mBtnAudio.getVisibility() == View.VISIBLE ? R.mipmap.ic_cheat_keyboard : R.mipmap.ic_cheat_voice);
    }

    private void showBtnAudio() {
        mBtnAudio.setVisibility(View.VISIBLE);
        mEtContent.setVisibility(View.GONE);
        mIvEmo.setVisibility(View.GONE);
        //????????????
        closeKeyBoardAndLoseFocus();
    }

    private void hideBtnAudio() {
        mBtnAudio.setVisibility(View.GONE);
        mEtContent.setVisibility(View.VISIBLE);
        mIvEmo.setVisibility(View.VISIBLE);
        //????????????
        openKeyBoardAndGetFocus();
    }

    private void showPlayAudio() {
        mBtnAudio.setText("?????? ??????");
        mBtnAudio.setBackgroundResource(R.drawable.shape_btn_voice_press);
    }

    private void hidePlayAudio() {
        mBtnAudio.setText("?????? ??????");
        mBtnAudio.setBackgroundResource(R.drawable.shape_btn_voice_normal);
        mFlPlayAudio.setVisibility(View.GONE);
    }

    /**
     * ????????????????????????????????????????????????????????????
     */
    private void updateTimerTip(boolean cancel) {
        if (cancel) {
            mTvTimerTip.setText("???????????????????????????");
            mTvTimerTip.setBackgroundResource(R.drawable.shape_bottom_corner_solid_red);
            mBtnAudio.setText("???????????????????????????");
        } else {
            mTvTimerTip.setText("???????????????????????????");
            mTvTimerTip.setBackgroundResource(0);
            mBtnAudio.setText("?????? ??????");
        }
    }

    /**
     * ????????????????????????
     */
    private void startAudioRecordAnim() {
        mFlPlayAudio.setVisibility(View.VISIBLE);
        mCTimer.setBase(SystemClock.elapsedRealtime());//????????????
        mCTimer.start();
    }

    /**
     * ????????????????????????
     */
    private void stopAudiioRecordAnim() {
        mFlPlayAudio.setVisibility(View.GONE);
        mCTimer.stop();
        mCTimer.setBase(SystemClock.elapsedRealtime());//????????????
    }

    private static boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth()
                || event.getRawY() < location[1] - 40) {
            return true;
        }

        return false;
    }

    /**
     * ?????????AudioRecord
     */
    private void initAudioRecord() {
        if (mAudioRecorderHelper == null)
            mAudioRecorderHelper = new AudioRecorder(this, RecordType.AAC, AudioRecorder.DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND, this);
    }

    /**
     * ??????????????????
     */
    private void onStartAudioRecord() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mAudioRecorderHelper.startRecord();


        mStartRecord = mAudioRecorderHelper.isRecording();
        mCanclled = false;
        if (!mTouched) {
            return;
        }

        showPlayAudio();
        updateTimerTip(false);
        startAudioRecordAnim();
    }

    /**
     * ??????????????????
     */
    private void onEndAudioRecord(boolean cancel) {
        getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAudioRecorderHelper.completeRecord(cancel);
        hidePlayAudio();
        stopAudiioRecordAnim();
    }

    /**
     * ??????????????????
     */
    private void cancelAudioRecord(boolean cancel) {
        if (!mStartRecord) {
            return;
        }

        if (mCanclled == cancel) {
            return;
        }

        mCanclled = cancel;
        updateTimerTip(cancel);
    }

    @Override
    public void onRecordReady() {

    }

    @Override
    public void onRecordStart(File audioFile, RecordType recordType) {

    }

    @Override
    public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
        sendAudioMsg(audioFile, audioLength);
    }

    @Override
    public void onRecordFail() {

    }

    @Override
    public void onRecordCancel() {

    }

    @Override
    public void onRecordReachedMaxTime(final int maxTime) {
        stopAudiioRecordAnim();
        showMaterialDialog("", "??????????????????????????????????????????", "??????", "??????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioRecorderHelper.handleEndRecord(true, maxTime);
                hideMaterialDialog();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMaterialDialog();
            }
        });
    }

    public boolean isRecording() {
        return mAudioRecorderHelper != null && mAudioRecorderHelper.isRecording();
    }

    public void requestPermission() {
        PermissionGen.with(this)
                .addRequestCode(100)
                .permissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO)
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = 100)
    public void doSomething() {
//        UIUtils.showToast("???????????????????????????????????????????????????");
    }

    @PermissionFail(requestCode = 100)
    public void doFailSomething() {
        UIUtils.showToast("???????????????????????????????????????????????????");
    }

    /*================== ?????????????????? end ==================*/
    /*================== ?????????????????? begin ==================*/

    @Override
    public void onBackPressed() {
        if (mIsPlayVideoShown) {
            hidePlayVideo();
            return;
        }
        super.onBackPressed();
    }

    private boolean mIsPlayVideoShown = false;//?????????????????????????????????

    public void showPlayVideo() {
        mLlPlayVideo.setVisibility(View.VISIBLE);
        initVideoRecord();
        mIsPlayVideoShown = true;
    }

    public void hidePlayVideo() {
        mLlPlayVideo.setVisibility(View.GONE);
        releaseVideoRecord();
        mIsPlayVideoShown = false;
        cvTouch();
    }

    public void initVideoRecord() {
        UIUtils.postTaskDelay(new Runnable() {
            @Override
            public void run() {
                mVrvVideo.openCamera();
            }
        }, 1000);
    }

    public void releaseVideoRecord() {
        mVrvVideo.stop();
    }

    /**
     * ???????????????????????????????????????????????????
     */
    public void resetVideoRecord() {
        mVrvVideo.stop();
        mVrvVideo.openCamera();
    }

    @Override
    public void onRecrodFinish() {
        UIUtils.postTaskSafely(new Runnable() {
            @Override
            public void run() {
                mTvTipOne.setVisibility(View.GONE);
                mTvTipTwo.setVisibility(View.GONE);
                resetVideoRecord();
                //????????????
                sendVidoMsg(mVrvVideo.getVecordFile(), mVrvVideo.getVecordFile().getName());
            }
        });
    }

    @Override
    public void onRecording(int timeCount, int recordMaxTime) {

    }

    @Override
    public void onRecordStart() {

    }

    /*================== ?????????????????? end ==================*/
    /*================== ??????????????????????????????????????? begin ==================*/
    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        loadHistoryMsgFromRemote();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }


    /*================== ??????????????????????????????????????? end ==================*/
}
