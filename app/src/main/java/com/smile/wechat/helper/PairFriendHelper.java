package com.smile.wechat.helper;


import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.smile.wechat.bmob.BmobManager;
import com.smile.wechat.bmob.FateSet;
import com.smile.wechat.model.UserCache;
import com.smile.wechat.nimsdk.NimUserInfoSDK;
import com.smile.wechat.utils.CommonUtils;
import com.smile.wechat.utils.LogUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * FileName: PairFriendHelper
 * Profile: 匹配好友
 */
public class PairFriendHelper {


    private static volatile com.smile.wechat.helper.PairFriendHelper mInstance = null;

    //延迟时间 单位s
    private static final int DELAY_TIME = 2;

    //随机类
    private Random mRandom;
    //自己的ID
    private String meUserId;
    //自己的对象

    private NimUserInfo meIMUser;

    //RxJava
    private Disposable mDisposable;

    //轮询次数
    private int FateNumber = 0;

    //接口
    private OnPairResultListener onPairResultListener;

    public void setOnPairResultListener(OnPairResultListener onPairResultListener) {
        this.onPairResultListener = onPairResultListener;
    }

    private PairFriendHelper() {

        mRandom = new Random();
        meIMUser = NimUserInfoSDK.getUser(UserCache.getAccount());
        meUserId = meIMUser.getAccount();
    }

    public static com.smile.wechat.helper.PairFriendHelper getInstance() {
        if (mInstance == null) {
            synchronized (com.smile.wechat.helper.PairFriendHelper.class) {
                if (mInstance == null) {
                    mInstance = new com.smile.wechat.helper.PairFriendHelper();
                }
            }
        }
        return mInstance;
    }
    /**
     * 缘分匹配好友
     */
    private void fateUser() {

        /**
         * 1.创建库
         * 2.将自己添加进去
         * 3.轮询查找好友
         * 4.10s
         * 5.查询到了之后则反馈给外部
         * 6.将自己删除
         */

        BmobManager.getInstance().addFateSet(new SaveListener<String>() {

            @Override
            public void done(final String s, BmobException e) {
                if (e == null) {
                    //轮询
                    mDisposable = Observable.interval(1, TimeUnit.SECONDS)
                              .subscribe(new Consumer<Long>() {
                                  @Override
                                  public void accept(Long aLong) throws Exception {
                                      queryFateSet(s);
                                  }
                              });
                }
            }
        });
    }

    /**
     * 查询缘分池
     *
     * @param id
     */
    private void queryFateSet(final String id) {
        BmobManager.getInstance().queryFateSet(new FindListener<FateSet>() {
            @Override
            public void done(List<FateSet> mlist, BmobException e) {
                FateNumber++;
                if (e == null) {
                    if (CommonUtils.isEmpty(mlist)) {
                        //如果>1才说明有人在匹配
                        if (mlist.size() > 2) {
                            disposable();
                            //过滤自己
                            for (int i = 0; i < mlist.size(); i++) {
                                FateSet fateSet = mlist.get(i);
                                if (fateSet.getUserId().equals(meUserId)) {
                                    mlist.remove(i);
                                    break;
                                }
                            }
                            //最终结果
                            int _r = mRandom.nextInt(mlist.size());
                            onPairResultListener.OnPairListener(mlist.get(_r).getUserId());
                            //删除自己
                            deleteFateSet(id);
                            FateNumber = 0;
                        } else {
                            LogUtils.i("FateNumber:" + FateNumber);
                            //超时
                            if (FateNumber >= 10) {
                                disposable();
                                deleteFateSet(id);
                                onPairResultListener.OnPairFailListener();
                                FateNumber = 0;
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 删除指定的缘分池
     *
     * @param id
     */
    private void deleteFateSet(String id) {
        BmobManager.getInstance().delFateSet(id, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    LogUtils.i("Delete");
                }
            }
        });
    }

    /**
     * 0：从用户组随机抽取一位好友
     * 1：深度匹配：资料的相似度
     * 2：缘分 同一时刻搜索的
     * 3：年龄相似的异性
     *
     * @param index
     */
    public void pairUser(int index, List<NimUserInfo> list) {
        switch (index) {
            case 0:
                randomUser(list);
                break;
            case 1:
                soulUser(list);
                break;
            case 2:
                fateUser();
                break;
            case 3:
                loveUser(list);
                break;
        }
    }

    /**
     * 恋爱匹配
     * @param list
     */
    private void loveUser(List<NimUserInfo> list) {
        /**
         * 1.抽取所有的用户
         * 2.根据性别抽取出异性
         * 3.根据年龄再抽取
         * 4.可以有一些附加条件：爱好 星座 ~~
         * 5.计算出来
         */

        List<NimUserInfo> _love_user = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NimUserInfo imUser = list.get(i);

            //过滤自己
            if (imUser.getAccount().equals(meUserId)) {
                //跳过本次循环
                continue;
            }

            //异性
            if (imUser.getGenderEnum() != meIMUser.getGenderEnum()) {
                _love_user.add(imUser);
            }
        }

        //异性保存成功
        if (CommonUtils.isEmpty(_love_user)) {
            final List<String> _love_id = new ArrayList<>();
            //计算年龄
            for (int i = 0; i < _love_user.size(); i++) {
                NimUserInfo imUser = _love_user.get(i);
                //匹配年龄
                try {
                    int  userAge = getAge(parse(imUser.getBirthday()));
                    int myAge=getAge(parse(meIMUser.getBirthday()));
                    if(Math.abs(userAge-myAge)<=3){
                        _love_id.add(imUser.getAccount());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (_love_id.size() > 0) {

                //在这里增加更多的判断条件

                //延迟发送
                rxJavaParingResult(new OnRxJavaResultListener() {
                    @Override
                    public void rxJavaResult() {
                        int _r = mRandom.nextInt(_love_id.size());
                        onPairResultListener.OnPairListener(_love_id.get(_r));
                    }
                });
            }else {
                onPairResultListener.OnPairFailListener();
            }
        } else {
            onPairResultListener.OnPairFailListener();
        }
    }


    /**
     * 随机匹配好友
     *
     * @param list
     */
    private void randomUser(final List<NimUserInfo> list) {
        /**
         * 1.获取到全部的用户组
         * 2.过滤自己
         * 3.开始随机
         * 4.根据随机的数值拿到对应的对象ID
         * 5.接口回传
         */

        //过滤自己
        for (int i = 0; i < list.size(); i++) {
            //对象ID == 我的ID
            if (list.get(i).getAccount().equals(meUserId)) {
                list.remove(i);
            }
        }

        //处理结果
        rxJavaParingResult(new OnRxJavaResultListener() {
            @Override
            public void rxJavaResult() {
                //接收2s后的通知
                //随机数
                int _r = mRandom.nextInt(list.size());
                NimUserInfo imUser = list.get(_r);
                if (imUser != null) {
                    onPairResultListener.OnPairListener(imUser.getAccount());
                }
            }
        });
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
            }else{
                age--;
            }
        }
        return age;
    }

    /**
     * 根据时间得到相应的星座
     * @param date 时间类型 格式 2021-05-26
     * @return 返回相应的星座
     */
    public static String getCons(String date){
        String cons[] = {"白羊座","金牛座","双子座","巨蟹座","狮子座","处女座","天秤座","天蝎座","射手座","摩羯座","水瓶座","双鱼座"};
        int edgeDate[] = {21,20,21,22,23,23,23,24,23,22,20,19};//星座起始日期 3月份开始

        String dates[] = date.split("-");
        if(dates.length<3) return null;//格式不正确
        int month = Integer.parseInt(dates[1]);
        int day = Integer.parseInt(dates[2]);

        int index = (month+9)%12;
        index = edgeDate[index]>day?(month+8)%12:index;//得到星座
        return cons[index];
    }

    /**
     * 灵魂匹配好友
     *
     * @param list
     */
    private void soulUser(List<NimUserInfo> list) {
        List<String> _list_objectId = new ArrayList<>();
        // 四要素：星座 年龄 爱好 状态
        for (int i = 0; i < list.size(); i++) {
            NimUserInfo imUser = list.get(i);

            //过滤自己
            if (imUser.getAccount().equals(meUserId)) {
                //跳过本次循环
                continue;
            }

            //匹配星座
            if (getCons(imUser.getBirthday())==getCons(meIMUser.getBirthday())) {
              _list_objectId.add(imUser.getAccount());
           }
            //匹配年龄
            try {
                int  userAge = getAge(parse(imUser.getBirthday()));
                int myAge=getAge(parse(meIMUser.getBirthday()));

                if(Math.abs(userAge-myAge)<=2){
                    _list_objectId.add(imUser.getAccount());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //匹配爱好
//            if (imUser.getHobby().equals(meIMUser.getHobby())) {
//                _list_objectId.add(imUser.getObjectId());
//            }

            //单身状态
//            if (imUser.getStatus().equals(meIMUser.getStatus())) {
//                _list_objectId.add(imUser.getObjectId());
//            }
        }

        //计算重复的ID

        /**
         * JAVA问题：List中重复的ID如何计算？
         */

        //定义Map
        Map<String, Integer> _map = new HashMap<>();
        //遍历List
        for (String str : _list_objectId) {
            //定义基础的次数
            Integer i = 1;
            //根据ID获取map的键值 如果不等于空 说明有数据 并且在原基础上自增1
            if (_map.get(str) != null) {
                i = _map.get(str) + 1;
            }
            //如果等于空？
            _map.put(str, i);
        }

        //LogUtils.i("_map:" + _map.toString());

        //如何获得最佳的对象？
        final List<String> _soul_list = mapComperTo(4, _map);

        //LogUtils.i("_soul_list:" + _soul_list.toString());

        if (CommonUtils.isEmpty(_soul_list)) {
            //计算
            rxJavaParingResult(new OnRxJavaResultListener() {
                @Override
                public void rxJavaResult() {
                    //随机数
                    int _r = mRandom.nextInt(_soul_list.size());
                    onPairResultListener.OnPairListener(_soul_list.get(_r));
                }
            });
        } else {
            onPairResultListener.OnPairFailListener();
        }
    }

    /**
     * Map计算将传入的size对应的key传出
     *
     * @param size
     * @param _map
     */
    private List<String> mapComperTo(int size, Map<String, Integer> _map) {
        List<String> _list_key = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : _map.entrySet()) {
            String _key = entry.getKey();
            Integer _values = entry.getValue();
            if (_values == size) {
                _list_key.add(_key);
            }
        }
        if (_list_key.size() == 0) {
            size = size - 1;
            if (size == 0) {
                return null;
            }
            return mapComperTo(size, _map);
        }
        return _list_key;
    }

    /**
     * 异步线程处理结果
     *
     * @param listener
     */
    private void rxJavaParingResult(final OnRxJavaResultListener listener) {
        //延迟
        mDisposable = Observable
                .timer(DELAY_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        listener.rxJavaResult();
                    }
                });
    }

    public interface OnPairResultListener {

        //匹配成功
        void OnPairListener(String userId);

        //匹配失败
        void OnPairFailListener();
    }

    public interface OnRxJavaResultListener {

        void rxJavaResult();
    }

    /**
     * 销毁
     */
    public void disposable() {
        if (mDisposable != null) {
            if(!mDisposable.isDisposed()){
                mDisposable.dispose();
            }
        }
    }
}
