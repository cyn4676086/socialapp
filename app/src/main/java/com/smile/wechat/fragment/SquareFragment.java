package com.smile.wechat.fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.smile.wechat.AppConst;
import com.smile.wechat.R;
import com.smile.wechat.activity.NearbyPerpleActivity;
import com.smile.wechat.activity.PushSquareActivity;
import com.smile.wechat.activity.ScanActivity;
import com.smile.wechat.activity.UserInfoActivity;
import com.smile.wechat.activity.WebViewActivity;
import com.smile.wechat.adapter.CommonAdapter;
import com.smile.wechat.adapter.CommonViewHolder;
import com.smile.wechat.bmob.BmobManager;
import com.smile.wechat.bmob.SquareSet;
import com.smile.wechat.helper.FileHelper;
import com.smile.wechat.helper.WindowHelper;
import com.smile.wechat.manager.MediaPlayerManager;
import com.smile.wechat.manager.VideoJzvdStd;
import com.smile.wechat.utils.AnimUtils;
import com.smile.wechat.utils.CommonUtils;
import com.smile.wechat.utils.TimeUtils2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @创建者 CYN
 * @描述 发现
 */
public class SquareFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * 1.设计并且实现云数据库 SquareSet
     * 2.实现我们的媒体发送 PsuhSquareActivity
     * 3.实现列表 并且实现我们的文本和图片的发送
     */

    private static final int REQUEST_CODE = 1000;

    private ImageView iv_push;
    private RecyclerView mSquareView;
    private SwipeRefreshLayout mSquareSwipeLayout;
    private View item_empty_view;

    //悬浮按钮
    private FloatingActionButton fb_squaue_top;

    private List<SquareSet> mList = new ArrayList<>();
    private CommonAdapter<SquareSet> mSquareAdapter;

    private SimpleDateFormat dateFormat;

    //播放
    private MediaPlayerManager mMusicManager;
    //音乐是否在播放
    private boolean isMusicPlay = false;

    //音乐悬浮窗
    private WindowManager.LayoutParams lpMusicParams;
    private View musicWindowView;
    private ImageView iv_music_photo;
    private ProgressBar pb_music_pos;
    private TextView tv_music_cur;
    private TextView tv_music_all;

    //是否移动
    private boolean isMove = false;
    //是否拖拽
    private boolean isDrag = false;
    private int mLastX;
    private int mLastY;

    //属性动画
    private ObjectAnimator objAnimMusic;

    //更新进度
    private static final int UPDATE_POS = 1235;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_POS:
                    int pos = msg.arg1;
                    tv_music_cur.setText(TimeUtils2.formatDuring(pos));
                    pb_music_pos.setProgress(pos);
                    break;
            }
            return false;
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_square, null);
        initView(view);
        return view;
    }

    @Override
    public View initView() {
        return null;
    }


    /**
     * 初始化View
     *
     * @param view
     */
    public void initView(final View view) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        initMusicWindow();

        mMusicManager = new MediaPlayerManager();
        mMusicManager.setOnComplteionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isMusicPlay = false;
            }
        });

        mMusicManager.setOnProgressListener(new MediaPlayerManager.OnMusicProgressListener() {
            @Override
            public void OnProgress(int progress, int pos) {
                Message message = new Message();
                message.what = UPDATE_POS;
                message.arg1 = progress;
                mHandler.sendMessage(message);
            }
        });

        iv_push = view.findViewById(R.id.iv_push);
        mSquareView = view.findViewById(R.id.mSquareView);
        mSquareSwipeLayout = view.findViewById(R.id.mSquareSwipeLayout);
        item_empty_view = view.findViewById(R.id.item_empty_view);
        fb_squaue_top = view.findViewById(R.id.fb_squaue_top);

        iv_push.setOnClickListener(this);
        fb_squaue_top.setOnClickListener(this);
        mSquareSwipeLayout.setOnRefreshListener(this);

        mSquareView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSquareView.addItemDecoration(new DividerItemDecoration(getActivity(),
                  DividerItemDecoration.VERTICAL));
        //取消动画
        ((SimpleItemAnimator) mSquareView.getItemAnimator()).setSupportsChangeAnimations(false);

        mSquareAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnMoreBindDataListener<SquareSet>() {
            @Override
            public int getItemType(int position) {
                return position;
            }

            @Override
            public void onBindViewHolder(final SquareSet model, CommonViewHolder viewHolder, int type, int position) {
                //加载个人信息
                NimUserInfo imUser = NIMClient.getService(UserService.class).getUserInfo(model.getUserId());
                if (!TextUtils.isEmpty(imUser.getAvatar())) {
                    viewHolder.setImageUrl(getActivity(), R.id.iv_photo, imUser.getAvatar(), 50, 50);
                }
                viewHolder.setText(R.id.tv_nickname, imUser.getName());
                try {
                    int age=getAge(parse(imUser.getBirthday()));
                    viewHolder.setText(R.id.tv_square_age,String.valueOf(age)+"岁");
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


//                                //有些属性没有，则隐藏
//                                String constellation = imUser.getConstellation();
//                                if (!TextUtils.isEmpty(constellation)) {
//                                    viewHolder.setText(R.id.tv_square_constellation, constellation);
//                                    viewHolder.setVisibility(R.id.tv_square_constellation, View.VISIBLE);
//                                }
//
//                                String hobby = imUser.getHobby();
//                                if (!TextUtils.isEmpty(hobby)) {
//                                    viewHolder.setText(R.id.tv_square_hobby, getString(R.string.text_squate_love) + hobby);
//                                    viewHolder.setVisibility(R.id.tv_square_hobby, View.VISIBLE);
//                                }
//                                String status = imUser.getStatus();
//                                if (!TextUtils.isEmpty(status)) {
//                                    viewHolder.setText(R.id.tv_square_status, imUser.getStatus());
//                                    viewHolder.setVisibility(R.id.tv_square_status, View.VISIBLE);
//                                }




                //设置时间
                viewHolder.setText(R.id.tv_time, dateFormat.format(model.getPushTime()));

                //设置头像点击事件
                viewHolder.getView(R.id.iv_photo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //跳转到用户信息界面
                        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                        intent.putExtra("account", model.getUserId());
                        startActivity(intent);
                    }
                });

                if (!TextUtils.isEmpty(model.getText())) {
                    viewHolder.setText(R.id.tv_text, model.getText());
                } else {
                    viewHolder.setVisibility(R.id.tv_text, View.GONE);
                }
                System.out.println(imUser.getAvatar());
                //多媒体
                switch (model.getPushType()) {
                    case SquareSet.PUSH_TEXT:
                        goneItemView(viewHolder, false, false, false);
                        break;
                    case SquareSet.PUSH_IMAGE:
                        goneItemView(viewHolder, true, false, false);
                        viewHolder.setImageUrl(getActivity(), R.id.iv_img, model.getMediaUrl());
                        viewHolder.getView(R.id.iv_img).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //ImagePreviewActivity.startActivity(getActivity(), true, model.getMediaUrl());
                            }
                        });
                        break;
                    case SquareSet.PUSH_MUSIC:
                        goneItemView(viewHolder, false, true, false);
                        viewHolder.getView(R.id.ll_music).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                    //播放音乐
                                    if (mMusicManager.isPlaying()) {
                                        hideMusicWindow();
                                    } else {
                                        if (isMusicPlay) {
                                            mMusicManager.continuePlay();
                                        } else {
                                            mMusicManager.startPlay(model.getMediaUrl());
                                            isMusicPlay = true;
                                        }
                                        showMusicWindow();
                                    }

                            }
                        });
                        break;
                    case SquareSet.PUSH_VIDEO:
                        goneItemView(viewHolder, false, false, true);
                        viewHolder.setVisibility(R.id.tv_text, View.GONE);

                        //实现我们的视频
                        final VideoJzvdStd jzvdStd = viewHolder.getView(R.id.jz_video);
                        jzvdStd.setUp(model.getMediaUrl(), model.getText());
                        Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
                            Bitmap mBitmap = FileHelper.getInstance()
                                      .getNetVideoBitmap(model.getMediaUrl());
                            if (mBitmap != null) {
                                emitter.onNext(mBitmap);
                                emitter.onComplete();
                            }
                        }).subscribeOn(Schedulers.newThread())
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .subscribe(bitmap -> {
                                      if (bitmap != null) {
                                          jzvdStd.thumbImageView.setImageBitmap(bitmap);
                                      }
                                  });
                        break;
                }

            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_square_item;
            }
        });
        mSquareView.setAdapter(mSquareAdapter);

        //监听列表滑动
        mSquareView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                        int position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                        if(position > 5 ){
                            fb_squaue_top.setVisibility(View.VISIBLE);
                        }else {
                            fb_squaue_top.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (!CommonUtils.isEmpty(mList)) {
                loadSquare();
            }
        }
    }

    /**
     * 初始化音乐悬浮窗
     */
    private void initMusicWindow() {
        lpMusicParams = WindowHelper.getInstance().createLayoutParams(
                  WindowManager.LayoutParams.WRAP_CONTENT,
                  WindowManager.LayoutParams.WRAP_CONTENT,
                  Gravity.TOP | Gravity.START);
        musicWindowView=View.inflate(getContext(), R.layout.layout_square_music_item, null);
        //初始化View
        iv_music_photo = musicWindowView.findViewById(R.id.iv_music_photo);
        pb_music_pos = musicWindowView.findViewById(R.id.pb_music_pos);
        tv_music_cur = musicWindowView.findViewById(R.id.tv_music_cur);
        tv_music_all = musicWindowView.findViewById(R.id.tv_music_all);

        objAnimMusic = AnimUtils.rotation(iv_music_photo);

        musicWindowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMusicWindow();
            }
        });

        musicWindowView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int mStartX = (int) event.getRawX();
                int mStartY = (int) event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isMove = false;
                        isDrag = false;
                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:

                        //偏移量
                        int dx = mStartX - mLastX;
                        int dy = mStartY - mLastY;

                        if (isMove) {
                            isDrag = true;
                        } else {
                            if (dx == 0 && dy == 0) {
                                isMove = false;
                            } else {
                                isMove = true;
                                isDrag = true;
                            }
                        }

                        //移动
                        lpMusicParams.x += dx;
                        lpMusicParams.y += dy;

                        //重置坐标
                        mLastX = mStartX;
                        mLastY = mStartY;

                        //WindowManager addView removeView updateView
                        WindowHelper.getInstance().updateView(musicWindowView, lpMusicParams);

                        break;
                }
                return isDrag;
            }
        });
    }

    /**
     * 显示窗口
     */
    private void showMusicWindow() {
        pb_music_pos.setMax(mMusicManager.getDuration());
        tv_music_all.setText(TimeUtils2.formatDuring(mMusicManager.getDuration()));
        objAnimMusic.start();
        WindowHelper.getInstance().showView(musicWindowView, lpMusicParams);
    }

    /**
     * 隐藏窗口
     */
    private void hideMusicWindow() {
        mMusicManager.pausePlay();
        objAnimMusic.pause();
        WindowHelper.getInstance().hideView(musicWindowView);
    }

    /**
     * 隐藏View
     *
     * @param viewHolder
     * @param img
     * @param audio
     * @param video
     */
    private void goneItemView(CommonViewHolder viewHolder,
                              boolean img, boolean audio, boolean video) {
        viewHolder.getView(R.id.tv_text).setVisibility(View.VISIBLE);
        viewHolder.getView(R.id.iv_img).setVisibility(img ? View.VISIBLE : View.GONE);
        viewHolder.getView(R.id.ll_music).setVisibility(audio ? View.VISIBLE : View.GONE);
        viewHolder.getView(R.id.ll_video).setVisibility(video ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_push:
                Intent intent = new Intent(getContext(), PushSquareActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.fb_squaue_top:
                mSquareView.smoothScrollToPosition(0);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                //刷新
                loadSquare();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 加载数据
     */
    private void loadSquare() {
        mSquareSwipeLayout.setRefreshing(true);
        BmobManager.getInstance().queryAllSquare(new FindListener<SquareSet>() {
            @Override
            public void done(List<SquareSet> list, BmobException e) {
                mSquareSwipeLayout.setRefreshing(false);
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        //倒序
                        Collections.reverse(list);
                        mSquareView.setVisibility(View.VISIBLE);
                        item_empty_view.setVisibility(View.GONE);
                        if (mList.size() > 0) {
                            mList.clear();
                        }
                        mList.addAll(list);
                        mSquareAdapter.notifyDataSetChanged();
                    } else {
                        mSquareView.setVisibility(View.GONE);
                        item_empty_view.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        loadSquare();
    }

    /**
     * 计算年龄
     *
     * @param
     */
    public static Date parse(String strDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(strDate);
    }
    //由出生日期获得年龄
    public static  int getAge(Date birthDay) throws Exception {
        if (birthDay == null) {
            return 100;
        } else {
            Calendar cal = Calendar.getInstance();
            if (cal.before(birthDay)) {
                throw new IllegalArgumentException(
                          "The birthDay is before Now.It's unbelievable!");
            }
            int yearNow = cal.get(Calendar.YEAR);
            int monthNow = cal.get(Calendar.MONTH);
            int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
            cal.setTime(birthDay);

            int yearBirth = cal.get(Calendar.YEAR);
            int monthBirth = cal.get(Calendar.MONTH);
            int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

            int age = yearNow - yearBirth;

            if (monthNow <= monthBirth) {
                if (monthNow == monthBirth) {
                    if (dayOfMonthNow < dayOfMonthBirth) age--;
                } else {
                    age--;
                }
            }
            return age;
        }
    }
}