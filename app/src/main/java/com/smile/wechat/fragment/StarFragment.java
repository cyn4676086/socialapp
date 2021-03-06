package com.smile.wechat.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.moxun.tagcloudlib.view.TagCloudView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.smile.wechat.AppConst;
import com.smile.wechat.R;
import com.smile.wechat.activity.SearchUserActivity;
import com.smile.wechat.activity.UserInfoActivity;
import com.smile.wechat.adapter.CloudTagAdapter;
import com.smile.wechat.bmob.ActiveUsersSet;
import com.smile.wechat.bmob.BmobManager;
import com.smile.wechat.bmob.FateSet;
import com.smile.wechat.helper.PairFriendHelper;
import com.smile.wechat.model.StarModel;
import com.smile.wechat.model.UserCache;
import com.smile.wechat.nimsdk.NimUserInfoSDK;
import com.smile.wechat.nimsdk.utils.ToastUtils;
import com.smile.wechat.utils.CommonUtils;
import com.smile.wechat.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class StarFragment extends BaseFragment implements View.OnClickListener {



    private TagCloudView mCloudView;

    private LinearLayout ll_random;
    private LinearLayout ll_soul;
    private LinearLayout ll_fate;
    private LinearLayout ll_love;

    private CloudTagAdapter mCloudTagAdapter;
    private final List<StarModel> mStarList=new ArrayList<>();
    private List<NimUserInfo> mAllUserList=new ArrayList<>();

    public StarFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_star, null);
        initViews(view);
        return view;
    }


    @Override
    public View initView(){
        return getView();
    }


    public void initViews(View view) {
        mCloudView = view.findViewById(R.id.mCloudView);

        ll_random = view.findViewById(R.id.ll_random);
        ll_soul = view.findViewById(R.id.ll_soul);
        ll_fate = view.findViewById(R.id.ll_fate);
        ll_love = view.findViewById(R.id.ll_love);

        ll_random.setOnClickListener(this);
        ll_soul.setOnClickListener(this);
        ll_fate.setOnClickListener(this);
        ll_love.setOnClickListener(this);



        //????????????
        mCloudTagAdapter=new CloudTagAdapter(getActivity(),mStarList);
        mCloudView.setAdapter(mCloudTagAdapter);
        //??????????????????
        mCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, int position) {
                startUserInfo(mStarList.get(position).getUserId());
            }
        });
        //??????????????????
        PairFriendHelper.getInstance().setOnPairResultListener(new PairFriendHelper.OnPairResultListener() {
            @Override
            public void OnPairListener(String userId) {
                hideWaitingDialog();
                startUserInfo(userId);
            }

            @Override
            public void OnPairFailListener() {
                hideWaitingDialog();
                ToastUtils.showMessage(getContext(),"????????????????????????????????????~");
            }
        });


        loadStarUser();

    }
    /*
     *??????????????????
     */
    public void startUserInfo(String userId){
        if (userId.equals(UserCache.getAccount())){
            ToastUtils.showMessage(getContext(),"????????????????????????????????????~");
        }else {
            //???????????????????????????
            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
            intent.putExtra("account", userId);
            startActivity(intent);
        }
    }
    private void saveStarUser(String userId, String nickName, String photoUrl){
        StarModel model=new StarModel();
        model.setNickName(nickName);
        model.setPhotoUrl(photoUrl);
        model.setUserId(userId);
        mStarList.add(model);
    }
    /**
     * ??????????????????
     */
    private void loadStarUser() {
        /**
         * ????????????????????????????????????,???????????????????????????????????????????????????????????????
         * ??????????????????????????????????????????????????????????????????
         * ???????????????????????????????????????????????????
         */

        List<String>userAccounts=new ArrayList<>();

        BmobManager.getInstance().queryActiveUsersSet(new FindListener<ActiveUsersSet>() {
            @Override
            public void done(List<ActiveUsersSet> list, BmobException e) {
                for (int i = 0; i < list.size(); i++) {
                    //????????????
                    ActiveUsersSet activeUsersSet = list.get(i);
                    if (activeUsersSet.getUserId().equals(UserCache.getAccount())) {
                        list.remove(i);
                    }else {
                        userAccounts.add(activeUsersSet.getUserId());
                    }
                }
                NimUserInfoSDK.getUserInfosFormServer(userAccounts, new RequestCallback<List<NimUserInfo>>() {
                    @Override
                    public void onSuccess(List<NimUserInfo> users) {

                        if(CommonUtils.isEmpty(users)){
                            mAllUserList=users;
                            if(mStarList.size()>0){
                                mStarList.clear();
                            }

                            //????????????????????????????????????????????????????????????????????????
                            int index=100;
                            if(users.size()<=100) {
                                index = users.size();
                                //?????????100?????????
                            }

                            //????????????
                            for (int i = 0; i < index; i++) {
                                NimUserInfo user = users.get(i);
                                saveStarUser(user.getAccount(),user.getName(),user.getAvatar());
                            }
                            mCloudTagAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        UIUtils.showToast("???????????????????????????" + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        exception.printStackTrace();
                    }
                });

            }
        });



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_random:
                pairUser(0);
                break;
            case R.id.ll_soul:
                pairUser(1);
                //????????????
                break;
            case R.id.ll_fate:
                //????????????
                pairUser(2);
                break;
            case R.id.ll_love:
                pairUser(3);
                //????????????
                break;
        }
    }
    /**
     * ????????????
     *
     * @param index
     */
    private void pairUser(int index) {
        switch (index) {
            case 0:
                showWaitingDialog("???????????????...");
                break;
            case 1:
                showWaitingDialog("???????????????...");
                break;
            case 2:
                showWaitingDialog("???????????????...");
                break;
            case 3:
                showWaitingDialog("???????????????...");
                break;
        }
        if (CommonUtils.isEmpty(mAllUserList)) {
            //??????
            PairFriendHelper.getInstance().pairUser(index, mAllUserList);
        }else {
            ToastUtils.showMessage(getContext(),"?????????????????????????????????");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PairFriendHelper.getInstance().disposable();
    }
}
