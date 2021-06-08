package com.smile.wechat.bmob;

import android.content.Context;

import com.smile.wechat.AppConst;
import com.smile.wechat.model.UserCache;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;


public class BmobManager {

    private static final String BMOB_NEW_DOMAIN = "http://sdk.cilc.cloud/8/";

    private volatile static com.smile.wechat.bmob.BmobManager mInstance = null;

    private BmobManager() {

    }

    public static com.smile.wechat.bmob.BmobManager getInstance() {
        if (mInstance == null) {
            synchronized (com.smile.wechat.bmob.BmobManager.class) {
                if (mInstance == null) {
                    mInstance = new com.smile.wechat.bmob.BmobManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化Bmob
     *
     * @param mContext
     */
    public void initBmob(Context mContext) {
        //如果Bmob绑定独立域名，则需要在初始化之前重置
        Bmob.resetDomain(BMOB_NEW_DOMAIN);
        Bmob.initialize(mContext, AppConst.BMOB_SDK_ID);
    }



    public String getLocalUserId(){
        return UserCache.getAccount();
    }

    /**
     * 查询缘分池
     *
     * @param listener
     */
    public void queryFateSet(FindListener<FateSet> listener) {
        BmobQuery<FateSet> query = new BmobQuery<>();
        query.findObjects(listener);
    }


    /**
     * 添加到缘分池中
     *
     * @param listener
     */
    public void addFateSet(SaveListener<String> listener) {
        FateSet set = new FateSet();
        set.setUserId(getLocalUserId());
        set.save(listener);
    }

    /**
     * 删除缘分池
     *
     * @param id
     * @param listener
     */
    public void delFateSet(String id, UpdateListener listener) {
        FateSet set = new FateSet();
        set.setObjectId(id);
        set.delete(listener);
    }



    /**
     * 发布广场
     * @param mediaType
     * @param text
     * @param path
     * @param listener
     */
    public void pushSquare(int mediaType, String text, String path, SaveListener<String> listener) {
        com.smile.wechat.bmob.SquareSet squareSet = new com.smile.wechat.bmob.SquareSet();
        squareSet.setUserId(getLocalUserId());
        squareSet.setPushTime(System.currentTimeMillis());

        squareSet.setText(text);
        squareSet.setMediaUrl(path);
        squareSet.setPushType(mediaType);
        squareSet.save(listener);
    }
    /**
     * 查询所有的广场的数据
     *
     * @param listener
     */
    public void queryAllSquare(FindListener<com.smile.wechat.bmob.SquareSet> listener) {
        BmobQuery<com.smile.wechat.bmob.SquareSet> query = new BmobQuery<>();
        query.findObjects(listener);
    }



}