package com.smile.wechat.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smile.wechat.App;
import com.smile.wechat.AppConst;
import com.smile.wechat.R;
import com.smile.wechat.adapter.MainPagerAdapter;
import com.smile.wechat.broadcast.AuthBroadcastReceiver;
import com.smile.wechat.factory.PopupWindowFactory;
import com.smile.wechat.fragment.BaseFragment;
import com.smile.wechat.fragment.ContactsFragment;
import com.smile.wechat.fragment.DiscoveryFragment;
import com.smile.wechat.fragment.SquareFragment;
import com.smile.wechat.fragment.MeFragment;
import com.smile.wechat.fragment.MessageFragment;
import com.smile.wechat.fragment.StarFragment;
import com.smile.wechat.nimsdk.NimAccountSDK;
import com.smile.wechat.nimsdk.NimFriendSDK;
import com.smile.wechat.nimsdk.NimSystemSDK;
import com.smile.wechat.nimsdk.NimTeamSDK;
import com.smile.wechat.nimsdk.NimUserInfoSDK;
import com.smile.wechat.nimsdk.custom.CustomAttachParser;
import com.smile.wechat.utils.LogUtils;
import com.smile.wechat.utils.StringUtils;
import com.smile.wechat.utils.UIUtils;
import com.netease.nimlib.sdk.InvocationFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @????????? CYN
 * @?????? ?????????
 */
public class MainActivity extends BaseActivity {

    public static final int REQ_CLEAR_UNREAD = 100;

    private int exit = 0;
    private StarFragment mStarFragment;
    private MessageFragment mMessageFragment;
    private ContactsFragment mContactsFragment;
    private DiscoveryFragment mDiscoveryFragment;
    private MeFragment mMeFragment;
    private List<BaseFragment> mFragments;

    private PopupWindow mPopupWindow;

    private List<SystemMessage> items = new ArrayList<>();//????????????
    private static final boolean MERGE_ADD_FRIEND_VERIFY = true; // ???????????????????????????????????????????????????????????????????????????????????????????????????
    private Set<String> addFriendVerifyRequestAccounts = new HashSet<>(); // ?????????????????????????????????????????????????????????

    private AuthBroadcastReceiver mAuthBroadcastReceiver;
    private Observer<StatusCode> mOnlineStatusObserver;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.vpContent)
    ViewPager mVpContent;

    // ??????
    @BindView(R.id.llButtom)
    LinearLayout mLlBottom;

    @BindView(R.id.tvMessageNormal)
    TextView mTvMessageNormal;
    @BindView(R.id.tvMessagePress)
    TextView mTvMessagePress;
    @BindView(R.id.tvMessageTextNormal)
    TextView mTvMessageTextNormal;
    @BindView(R.id.tvMessageTextPress)
    TextView mTvMessageTextPress;
    @BindView(R.id.tvMessageCount)
    public TextView mTvMessageCount;

    @BindView(R.id.tvContactsNormal)
    TextView mTvContactsNormal;
    @BindView(R.id.tvContactsPress)
    TextView mTvContactsPress;
    @BindView(R.id.tvContactsTextNormal)
    TextView mTvContactsTextNormal;
    @BindView(R.id.tvContactsTextPress)
    TextView mTvContactsTextPress;
    @BindView(R.id.tvContactCount)
    public TextView mTvContactCount;

    @BindView(R.id.tvStarNormal)
    TextView mTvStarNormal;
    @BindView(R.id.tvStarPress)
    TextView mTvStarPress;
    @BindView(R.id.tvStarTextNormal)
    TextView mTvStarTextNormal;
    @BindView(R.id.tvStarTextPress)
    TextView mTvStarTextPress;
    @BindView(R.id.tvStarCount)
    public TextView mTvStarCount;

    @BindView(R.id.tvDiscoveryNormal)
    TextView mTvDiscoveryNormal;
    @BindView(R.id.tvDiscoveryPress)
    TextView mTvDiscoveryPress;
    @BindView(R.id.tvDiscoveryTextNormal)
    TextView mTvDiscoveryTextNormal;
    @BindView(R.id.tvDiscoveryTextPress)
    TextView mTvDiscoveryTextPress;
    @BindView(R.id.tvDiscoveryCount)
    public TextView mTvDiscoveryCount;

    @BindView(R.id.tvMeNormal)
    TextView mTvMeNormal;
    @BindView(R.id.tvMePress)
    TextView mTvMePress;
    @BindView(R.id.tvMeTextNormal)
    TextView mTvMeTextNormal;
    @BindView(R.id.tvMeTextPress)
    TextView mTvMeTextPress;
    @BindView(R.id.tvMeCount)
    public TextView mTvMeCount;


    @OnClick({R.id.llMessage, R.id.llContacts,R.id.llStar, R.id.llDiscovery, R.id.llMe})
    public void click(View view) {
        setTransparency();
        switch (view.getId()) {
            case R.id.llMessage:
                mVpContent.setCurrentItem(0, false);
                mTvMessagePress.getBackground().setAlpha(255);
                mTvMessageTextPress.setTextColor(Color.argb(255, 140,192,234));
                break;
            case R.id.llContacts:
                mVpContent.setCurrentItem(1, false);
                mTvContactsPress.getBackground().setAlpha(255);
                mTvContactsTextPress.setTextColor(Color.argb(255, 140,192,234));
                break;
            case R.id.llStar:
                mVpContent.setCurrentItem(2, false);
                mTvStarPress.getBackground().setAlpha(255);
                mTvStarTextPress.setTextColor(Color.argb(255, 140,192,234));
                break;
            case R.id.llDiscovery:
                mVpContent.setCurrentItem(3, false);
                mTvDiscoveryPress.getBackground().setAlpha(255);
                mTvDiscoveryTextPress.setTextColor(Color.argb(255, 140,192,234));
                break;
            case R.id.llMe:
                mVpContent.setCurrentItem(4, false);
                mTvMePress.getBackground().setAlpha(255);
                mTvMeTextPress.setTextColor(Color.argb(255, 140,192,234));
                break;
        }
    }

    @Override
    public void init() {
        //?????????????????????????????????
        registerBroadcastReceiver();
        //??????????????????
        observerLineStatus();
        //????????????????????????
        observeUserInfoUpdate();
        //???????????????????????????
        observeFriendChangedNotify();
        //???????????????????????????
        observeTeamChangedNotify();
        //????????????????????????
        observeReceiveSystemMsg();
        // ?????????????????????????????????
        NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new CustomAttachParser());
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolbar();

        //?????????????????????
        setTransparency();
        mTvMessagePress.getBackground().setAlpha(255);
        mTvMessageTextPress.setTextColor(Color.argb(255, 140,192,234));

        //??????ViewPager?????????????????????
        mVpContent.setOffscreenPageLimit(3);

    }

    @Override
    public void initData() {
        //??????5????????????Fragment
        mFragments = new ArrayList<>();
        mMessageFragment = new MessageFragment();
        mContactsFragment = new ContactsFragment();
        mStarFragment=new StarFragment();
        mDiscoveryFragment = new DiscoveryFragment();
        mMeFragment = new MeFragment();
        mFragments.add(mMessageFragment);
        mFragments.add(mContactsFragment);
        mFragments.add(mStarFragment);
        mFragments.add(mDiscoveryFragment);
        mFragments.add(mMeFragment);


        //??????????????????vp?????????
        mVpContent.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), mFragments));
        mVpContent.setCurrentItem(0);

        //???????????????????????????????????????????????????
        updateContactCount();
    }

    @Override
    public void initListener() {
        //??????vp????????????????????????????????????????????????
        mVpContent.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //??????ViewPager???????????????????????????
                int diaphaneity_one = (int) (255 * positionOffset);
                int diaphaneity_two = (int) (255 * (1 - positionOffset));
                switch (position) {
                    case 0:
                        mTvMessageNormal.getBackground().setAlpha(diaphaneity_one);
                        mTvMessagePress.getBackground().setAlpha(diaphaneity_two);
                        mTvContactsNormal.getBackground().setAlpha(diaphaneity_two);
                        mTvContactsPress.getBackground().setAlpha(diaphaneity_one);
                        mTvMessageTextNormal.setTextColor(Color.argb(diaphaneity_one, 153, 153, 153));
                        mTvMessageTextPress.setTextColor(Color.argb(diaphaneity_two, 140,192,234));
                        mTvContactsTextNormal.setTextColor(Color.argb(diaphaneity_two, 153, 153, 153));
                        mTvContactsTextPress.setTextColor(Color.argb(diaphaneity_one, 140,192,234));
                        break;
                    case 1:
                        mTvContactsNormal.getBackground().setAlpha(diaphaneity_one);
                        mTvContactsPress.getBackground().setAlpha(diaphaneity_two);
                        mTvStarNormal.getBackground().setAlpha(diaphaneity_two);
                        mTvStarPress.getBackground().setAlpha(diaphaneity_one);
                        mTvContactsTextNormal.setTextColor(Color.argb(diaphaneity_one, 153, 153, 153));
                        mTvContactsTextPress.setTextColor(Color.argb(diaphaneity_two, 140,192,234));
                        mTvStarTextNormal.setTextColor(Color.argb(diaphaneity_two, 153, 153, 153));
                        mTvStarTextPress.setTextColor(Color.argb(diaphaneity_one, 140,192,234));
                        break;
                    case 2:
                        mTvStarNormal.getBackground().setAlpha(diaphaneity_one);
                        mTvStarPress.getBackground().setAlpha(diaphaneity_two);
                        mTvDiscoveryNormal.getBackground().setAlpha(diaphaneity_two);
                        mTvDiscoveryPress.getBackground().setAlpha(diaphaneity_one);
                        mTvStarTextNormal.setTextColor(Color.argb(diaphaneity_one, 153, 153, 153));
                        mTvStarTextPress.setTextColor(Color.argb(diaphaneity_two, 140,192,234));
                        mTvDiscoveryTextNormal.setTextColor(Color.argb(diaphaneity_two, 153, 153, 153));
                        mTvDiscoveryTextPress.setTextColor(Color.argb(diaphaneity_one, 140,192,234));
                        break;
                    case 3:
                        mTvDiscoveryNormal.getBackground().setAlpha(diaphaneity_one);
                        mTvDiscoveryPress.getBackground().setAlpha(diaphaneity_two);
                        mTvMeNormal.getBackground().setAlpha(diaphaneity_two);
                        mTvMePress.getBackground().setAlpha(diaphaneity_one);
                        mTvDiscoveryTextNormal.setTextColor(Color.argb(diaphaneity_one, 153, 153, 153));
                        mTvDiscoveryTextPress.setTextColor(Color.argb(diaphaneity_two, 140,192,234));
                        mTvMeTextNormal.setTextColor(Color.argb(diaphaneity_two, 153, 153, 153));
                        mTvMeTextPress.setTextColor(Color.argb(diaphaneity_one, 140,192,234));
                        break;
                }

            }

            @Override
            public void onPageSelected(int position) {
                //???????????????????????????????????????????????????????????????
                if (position == 1) {
                    mContactsFragment.showQuickIndexBar(true);
                } else {
                    mContactsFragment.showQuickIndexBar(false);
                }

                //??????position????????????Fragment?????????
                mFragments.get(position).initData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state != ViewPager.SCROLL_STATE_IDLE) {
                    //????????????????????????????????????
                    mContactsFragment.showQuickIndexBar(false);
                } else {
                    mContactsFragment.showQuickIndexBar(true);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemSearch:
                Intent intent = new Intent(this, SearchUserActivity.class);
                intent.putExtra(SearchUserActivity.SEARCH_TYPE, SearchUserActivity.SEARCH_USER_REMOTE);
                startActivity(intent);
                break;
            case R.id.itemMore:
                showMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CLEAR_UNREAD) {
            updateContactCount();
        }
    }

    @Override
    protected void onDestroy() {
        unRegisterBroadcastReceiver();
        super.onDestroy();
    }

    private void initToolbar() {
        //??????ToolBar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Smile");
        mToolbar.setTitleTextColor(UIUtils.getColor(R.color.white));
    }

    private void showMenu() {
        View menuView = View.inflate(this, R.layout.popup_menu_main, null);
        //????????????
        menuView.findViewById(R.id.itemCreateGroupCheat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TeamCheatCreateActvitiy.class));
                mPopupWindow.dismiss();
            }
        });
        //????????????
        menuView.findViewById(R.id.itemAddFriend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, AddFriendActivity.class), MainActivity.REQ_CLEAR_UNREAD);
                mPopupWindow.dismiss();
            }
        });
        //?????????
        menuView.findViewById(R.id.itemScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ScanActivity.class));
                mPopupWindow.dismiss();
            }
        });
        //???????????????
        menuView.findViewById(R.id.itemHelpAndFeedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra("url", AppConst.Url.HELP_FEEDBACK);
                startActivity(intent);
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow = PopupWindowFactory.getPopupWindowAtLocation(menuView, mVpContent, Gravity.RIGHT | Gravity.TOP, UIUtils.dip2Px(12), mToolbar.getHeight() + getStatusBarHeight());
    }

    /**
     * ???press???????????????????????????(???????????????)
     */
    private void setTransparency() {
        mTvMessageNormal.getBackground().setAlpha(255);
        mTvContactsNormal.getBackground().setAlpha(255);
        mTvDiscoveryNormal.getBackground().setAlpha(255);
        mTvStarNormal.getBackground().setAlpha(255);
        mTvMeNormal.getBackground().setAlpha(255);

        mTvMessagePress.getBackground().setAlpha(1);
        mTvContactsPress.getBackground().setAlpha(1);
        mTvStarPress.getBackground().setAlpha(1);
        mTvDiscoveryPress.getBackground().setAlpha(1);
        mTvMePress.getBackground().setAlpha(1);

        mTvMessageTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvContactsTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvStarTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvDiscoveryTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvMeTextNormal.setTextColor(Color.argb(255, 153, 153, 153));

        mTvMessageTextPress.setTextColor(Color.argb(0, 69, 192, 26));
        mTvContactsTextPress.setTextColor(Color.argb(0, 69, 192, 26));
        mTvStarTextPress.setTextColor(Color.argb(0, 69, 192, 26));
        mTvDiscoveryTextPress.setTextColor(Color.argb(0, 69, 192, 26));
        mTvMeTextPress.setTextColor(Color.argb(0, 69, 192, 26));
    }

    /**
     * ???????????????????????????????????????????????????
     */
    public void updateContactCount() {
        //??????????????????????????? ???????????????
        List<SystemMessageType> types = new ArrayList<>();
        types.add(SystemMessageType.AddFriend);
        types.add(SystemMessageType.TeamInvite);
        int unreadCount = NimSystemSDK.querySystemMessageUnreadCountByType(types);
        if (unreadCount > 0) {
            mTvContactCount.setVisibility(View.VISIBLE);
            mTvContactCount.setText(String.valueOf(unreadCount));
            return;
        } else {
            mTvContactCount.setVisibility(View.GONE);
        }
    }

    /**
     * ??????????????????????????????2????????????2???????????????
     */
    @Override
    public void onBackPressed() {
        if (exit++ == 1) {
           App.exit();
        } else {
            UIUtils.showToast("??????????????????");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                   exit = 0;
                }
            }, 2000);
        }
    }

    /**
     * ?????????????????????
     */
    private void registerBroadcastReceiver() {
        //???????????????????????????
        mAuthBroadcastReceiver = new AuthBroadcastReceiver();
        registerReceiver(mAuthBroadcastReceiver, new IntentFilter(AuthBroadcastReceiver.ACTION));
    }

    /**
     * ????????????????????????
     */
    private void unRegisterBroadcastReceiver() {
        if (mAuthBroadcastReceiver != null) {
            unregisterReceiver(mAuthBroadcastReceiver);
            mAuthBroadcastReceiver = null;
        }
    }

    /**
     * ??????????????????
     */
    private void observerLineStatus() {
        mOnlineStatusObserver = new Observer<StatusCode>() {
            public void onEvent(StatusCode status) {
                LogUtils.sf("User status changed to: " + status);
                // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (status.wontAutoLogin()) {
                    //???????????????
                    Intent intent = new Intent();
                    intent.setAction(AuthBroadcastReceiver.ACTION);
                    intent.putExtra("status", status.getValue());
                    sendBroadcast(intent);
                }
            }
        };
        NimAccountSDK.onlineStatusListen(
                mOnlineStatusObserver, true);
    }

    /**
     * ????????????????????????
     */
    private void observeUserInfoUpdate() {
        NimUserInfoSDK.observeUserInfoUpdate(new Observer<List<NimUserInfo>>() {
            @Override
            public void onEvent(List<NimUserInfo> nimUserInfos) {
                mMeFragment.initData();
            }
        }, true);
    }

    /**
     * ???????????????????????????
     */
    private void observeFriendChangedNotify() {
        NimFriendSDK.observeFriendChangedNotify(new Observer<FriendChangedNotify>() {
            @Override
            public void onEvent(FriendChangedNotify friendChangedNotify) {
//                List<Friend> addedOrUpdatedFriends = friendChangedNotify.getAddedOrUpdatedFriends(); // ???????????????
//                List<String> deletedFriendAccounts = friendChangedNotify.getDeletedFriends(); // ?????????????????????????????????

                //?????????????????????
                mContactsFragment.initData();
            }
        }, true);
    }

    /**
     * ???????????????????????????
     */
    private void observeTeamChangedNotify() {
        NimTeamSDK.observeTeamRemove(new Observer<Team>() {
            @Override
            public void onEvent(Team team) {
                mMessageFragment.initData();
            }
        }, true);
//        NimTeamSDK.observeTeamUpdate(new Observer<List<Team>>() {
//            @Override
//            public void onEvent(List<Team> teams) {
//                mMessageFragment.initData();
//            }
//        }, true);
    }

    /**
     * ????????????????????????
     */
    private void observeReceiveSystemMsg() {
        NimSystemSDK.observeReceiveSystemMsg(new Observer<SystemMessage>() {
            @Override
            public void onEvent(final SystemMessage systemMessage) {

                items.clear();
                List<SystemMessageType> types = new ArrayList<>();
                types.add(SystemMessageType.AddFriend);
                types.add(SystemMessageType.TeamInvite);
                InvocationFuture<List<SystemMessage>> listInvocationFuture = NimSystemSDK.querySystemMessageByType(types, 0, 100);
                listInvocationFuture.setCallback(new RequestCallback<List<SystemMessage>>() {
                    @Override
                    public void onSuccess(List<SystemMessage> param) {
                        if (!StringUtils.isEmpty(param)) {
                            items.addAll(param);

                            //TODO:????????????????????????????????????????????????
                            SystemMessage del = null;
                            for (SystemMessage m : items) {
                                if (m.getMessageId() != systemMessage.getMessageId() &&
                                        m.getFromAccount().equals(systemMessage.getFromAccount()) && m.getType() == SystemMessageType.AddFriend) {
                                    del = m;
                                    break;
                                }
                            }
                            if (del != null) {
                                items.remove(del);
                                //???????????????????????????????????????
                                NimSystemSDK.deleteSystemMessage(del);
                            }

                            //?????????????????????????????????
                            updateContactCount();
                            mContactsFragment.updateHeaderViewUnreadCount();


                            //????????????????????????????????????
                            if (systemMessage.getType() == SystemMessageType.AddFriend) {
                                NimUserInfoSDK.getUserInfoFromServer(systemMessage.getFromAccount(), null);
                            }
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                    }

                    @Override
                    public void onException(Throwable exception) {
                    }
                });
            }
        }, true);
    }
}
