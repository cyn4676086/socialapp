package com.smile.wechat;


import android.os.Environment;

import com.smile.wechat.utils.LogUtils;

import java.io.File;

/**
 * @创建者 CYN
 * @描述 全局常量类
 */
public class AppConst {

    public static final String TAG = "CYN";
    public static final int DEBUGLEVEL = LogUtils.LEVEL_ALL;//日志输出级别
    public static final int CACHELTIMEOUT = 10 * 60 * 1000;// 10分钟(缓存过期时间)

    public static final String NETWORK_CHANGE_RECEIVED_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    public static final String SERVER_ADDRESS = "http://xxx.com/client";

    public static final class Account {
        public static final String KEY_USER_ACCOUNT = "account";
        public static final String KEY_USER_TOKEN = "token";
    }

    //二维码扫码指令前缀
    public static final class QRCodeCommend {
        public static final String ACCOUNT = "account:";
        public static final String TEAMID = "teamId:";
    }

    //用户
    public static final class User {
        private static final String USER = SERVER_ADDRESS + "/user";
        public static final String LOGIN = USER + "/login";//登录
        public static final String REGISTER = USER + "/insertOrUpdate";//注册
        public static final String WX_LOGIN = USER + "/androidWXLogin";//Smlie登录
    }

    public static final class Url {
        //帮助与反馈
        public static final String HELP_FEEDBACK = "https://kf.qq.com/touch/product/wechat_app.html?scene_id=kf338&code=021njRdi0RdQfk1Khybi0kEQdi0njRde&state=123";
        //购物
        public static final String SHOP = "http://wqs.jd.com/portal/wx/portal_indexV4.shtml?PTAG=17007.13.1&ptype=1";
        //游戏
        public static final String GAME = "https://sj.qq.com/";
    }

    //用户拓展信息字段
    public static final class UserInfoExt {
        public static final String AREA = "area";
        public static final String PHONE = "phone";
    }

    //我的群成员信息拓展字段
    public static final class MyTeamMemberExt {
        public static final String SHOULD_SHOW_NICK_NAME = "shouldShowNickName";
    }

    public static final String APP_KEY = "21b53caca2461392608406627238afc7";

    public static final String BMOB_SDK_ID = "5e9ab4b87f207e92df02531882f8d645";

    public static final String APP_SECURY = "0084a95d3afc";
    //是否第一次进入App
    public static final String SP_IS_FIRST_APP = "is_First_App";

    /**
     * SharePreference 相关
     */
    public static final String LOCAL_LOGIN_TABLE = "LOGIN_INFO";
    public static final String LOCAL_USER_ACCOUNT = "USER_ACOUNT";
    public static final String LOCAL_USER_TOKEN = "USER_TOKEN";

    public static final String OPTION_TABLE = "OPTION_TABLE";
    public static final String OPTION_KEYBOARD_HEIGHT = "OPTION_KEYBOARD_HEIGHT";

    /**
     * APP 缓存文件夹根目录
     */
    public static final String APP_CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
              + File.separator+"Smile";

    public static final String APP_CACHE_AUDIO = AppConst.APP_CACHE_PATH + File.separator + "audio";

    public static final String APP_CACHE_IMAGE = AppConst.APP_CACHE_PATH + File.separator + "image";

    public static final String APP_CACHE_VIDEO = AppConst.APP_CACHE_PATH + File.separator + "video";

}
