package com.smile.wechat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.netease.lava.nertc.sdk.NERtcCallback;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.NERtcParameters;
import com.netease.lava.nertc.sdk.video.NERtcRemoteVideoStreamType;
import com.netease.lava.nertc.sdk.video.NERtcVideoView;
import com.smile.wechat.AppConst;
import com.smile.wechat.R;

import java.util.Random;


public class MeetingActivity extends AppCompatActivity implements NERtcCallback, View.OnClickListener {

    private static final String TAG = "MeetingActivity";
    private static final String EXTRA_ROOM_ID = "extra_room_id";

    private boolean enableLocalVideo = true;
    private boolean enableLocalAudio = true;
    private boolean joinedChannel = false;

    private NERtcVideoView localUserVv;
    private NERtcVideoView remoteUserVv;
    private TextView waitHintTv;
    private ImageButton enableAudioIb;
    private ImageButton leaveIb;
    private ImageButton enableVideoIb;
    private ImageView cameraFlipImg;
    private View localUserBgV;

    public static void startActivity(Activity activity, String roomId) {
        Intent intent = new Intent(activity, com.smile.wechat.activity.MeetingActivity.class);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_meeting);
        initViews();
        setupNERtc();
        String roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        long userId = generateRandomUserID();
        joinChannel(userId, roomId);
    }

    /**
     * ????????????
     *
     * @param userID ??????ID
     * @param roomID ??????ID
     */
    private void joinChannel(long userID, String roomID) {
        Log.i(TAG, "joinChannel userId: " + userID);
        NERtcEx.getInstance().joinChannel(null, roomID, userID);
        localUserVv.setZOrderMediaOverlay(true);
        localUserVv.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_FIT);
        NERtcEx.getInstance().setupLocalVideoCanvas(localUserVv);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NERtcEx.getInstance().release();
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void initViews() {
        localUserVv = findViewById(R.id.vv_local_user);
        remoteUserVv = findViewById(R.id.vv_remote_user);
        waitHintTv = findViewById(R.id.tv_wait_hint);
        enableAudioIb = findViewById(R.id.ib_audio);
        leaveIb = findViewById(R.id.ib_leave);
        enableVideoIb = findViewById(R.id.ib_video);
        cameraFlipImg = findViewById(R.id.img_camera_flip);
        localUserBgV = findViewById(R.id.v_local_user_bg);

        localUserVv.setVisibility(View.INVISIBLE);
        enableAudioIb.setOnClickListener(this);
        leaveIb.setOnClickListener(this);
        enableVideoIb.setOnClickListener(this);
        cameraFlipImg.setOnClickListener(this);
    }

    /**
     * ?????????SDK
     */
    private void setupNERtc() {
        NERtcParameters parameters = new NERtcParameters();
        parameters.set(NERtcParameters.KEY_AUTO_SUBSCRIBE_AUDIO, false);
        NERtcEx.getInstance().setParameters(parameters); //??????????????????????????????

        try {
            NERtcEx.getInstance().init(getApplicationContext(), AppConst.APP_KEY, this, null);
        } catch (Exception e) {
            // ??????????????????release????????????????????????release???????????????
            NERtcEx.getInstance().release();
            try {
                NERtcEx.getInstance().init(getApplicationContext(), AppConst.APP_KEY, this, null);
            } catch (Exception ex) {
                Toast.makeText(this, "SDK???????????????", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        setLocalAudioEnable(true);
        setLocalVideoEnable(true);
    }

    /**
     * ??????????????????ID
     *
     * @return ??????ID
     */
    private int generateRandomUserID() {
        return new Random().nextInt(100000);
    }

    /**
     * ????????????
     *
     * @return ?????????
     * @see NERtcConstants.ErrorCode
     */
    private boolean leaveChannel() {
        joinedChannel = false;
        setLocalAudioEnable(false);
        setLocalVideoEnable(false);
        int ret = NERtcEx.getInstance().leaveChannel();
        return ret == NERtcConstants.ErrorCode.OK;
    }

    /**
     * ???????????????????????????
     */
    private void exit() {
        if (joinedChannel) {
            leaveChannel();
        }
        finish();
    }

    @Override
    public void onJoinChannel(int result, long channelId, long elapsed) {
        Log.i(TAG, "onJoinChannel result: " + result + " channelId: " + channelId + " elapsed: " + elapsed);

        if (result == NERtcConstants.ErrorCode.OK) {
            joinedChannel = true;
            // ???????????????????????????????????????
            localUserVv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLeaveChannel(int result) {
        Log.i(TAG, "onLeaveChannel result: " + result);
    }

    @Override
    public void onUserJoined(long uid) {
        Log.i(TAG, "onUserJoined uid: " + uid);
        // ?????????????????????????????????
        if (remoteUserVv.getTag() != null) {
            return;
        }
        // ????????????????????????Tag?????????????????????????????????????????????????????????
        remoteUserVv.setTag(uid);
        // ???????????????
        waitHintTv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onUserLeave(long uid, int reason) {
        Log.i(TAG, "onUserLeave uid: " + uid + " reason: " + reason);
        // ??????????????????????????????????????????????????????
        if (!isCurrentUser(uid)) {
            return;
        }
        // ??????TAG???null???????????????????????????
        remoteUserVv.setTag(null);
        NERtcEx.getInstance().subscribeRemoteVideoStream(uid, NERtcRemoteVideoStreamType.kNERtcRemoteVideoStreamTypeHigh, false);

        // ?????????????????????????????????
        waitHintTv.setVisibility(View.VISIBLE);
        // ???????????????
        remoteUserVv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onUserAudioStart(long uid) {
        Log.i(TAG, "onUserAudioStart uid: " + uid);
        if (!isCurrentUser(uid)) {
            return;
        }
        NERtcEx.getInstance().subscribeRemoteAudioStream(uid, true);
    }

    @Override
    public void onUserAudioStop(long uid) {
        Log.i(TAG, "onUserAudioStop, uid=" + uid);
    }

    @Override
    public void onUserVideoStart(long uid, int profile) {
        Log.i(TAG, "onUserVideoStart uid: " + uid + " profile: " + profile);
        if (!isCurrentUser(uid)) {
            return;
        }

        NERtcEx.getInstance().subscribeRemoteVideoStream(uid, NERtcRemoteVideoStreamType.kNERtcRemoteVideoStreamTypeHigh, true);
        remoteUserVv.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_FIT);
        NERtcEx.getInstance().setupRemoteVideoCanvas(remoteUserVv, uid);

        // ????????????
        remoteUserVv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserVideoStop(long uid) {
        Log.i(TAG, "onUserVideoStop, uid=" + uid);
        if (!isCurrentUser(uid)) {
            return;
        }
        // ???????????????
        remoteUserVv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDisconnect(int reason) {
        Log.i(TAG, "onDisconnect reason: " + reason);
        if (reason != NERtcConstants.ErrorCode.OK) {
            finish();
        }
    }

    /**
     * ???????????????onUserJoined???????????????Tag?????????
     *
     * @param uid ??????ID
     * @return ??????ID????????????
     */
    private boolean isCurrentUser(long uid) {
        Object tag = remoteUserVv.getTag();
        Log.i(TAG, "isCurrentUser tag=" + tag);
        return tag != null && tag.equals(uid);
    }

    /**
     * ??????????????????????????????
     */
    private void changeAudioEnable() {
        enableLocalAudio = !enableLocalAudio;
        setLocalAudioEnable(enableLocalAudio);
    }

    /**
     * ??????????????????????????????
     */
    private void changeVideoEnable() {
        enableLocalVideo = !enableLocalVideo;
        setLocalVideoEnable(enableLocalVideo);
    }

    /**
     * ??????????????????????????????
     */
    private void setLocalAudioEnable(boolean enable) {
        enableLocalAudio = enable;
        NERtcEx.getInstance().enableLocalAudio(enableLocalAudio);
        enableAudioIb.setImageResource(enable ? R.drawable.selector_meeting_mute : R.drawable.selector_meeting_unmute);
    }

    /**
     * ??????????????????????????????
     */
    private void setLocalVideoEnable(boolean enable) {
        enableLocalVideo = enable;
        NERtcEx.getInstance().enableLocalVideo(enableLocalVideo);
        enableVideoIb.setImageResource(enable ? R.drawable.selector_meeting_close_video : R.drawable.selector_meeting_open_video);
        localUserVv.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
        localUserBgV.setBackgroundColor(getResources().getColor(enable ? R.color.white : R.color.black));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_audio:
                changeAudioEnable();
                break;
            case R.id.ib_leave:
                exit();
                break;
            case R.id.ib_video:
                changeVideoEnable();
                break;
            case R.id.img_camera_flip:
                NERtcEx.getInstance().switchCamera();
                break;
            default:
                break;
        }
    }
}