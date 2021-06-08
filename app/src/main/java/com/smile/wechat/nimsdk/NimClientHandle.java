package com.smile.wechat.nimsdk;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smile.wechat.AppConst;
import com.smile.wechat.nimsdk.utils.CheckSumUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NimClientHandle {

    private static final String TAG = NimClientHandle.class.getSimpleName();

    private final String APP_SERVER_BASE_URL = "https://api.netease.im/nimserver/";

    private final String mAppServerUserCreate = "user/create.action";
    private final String mAppServerUserUpdate = "user/update.action";


    private static NimClientHandle instance;
    private OkHttpClient mOkHttpClient;

    public static NimClientHandle getInstance() {
        if (instance == null) {
            synchronized (NimClientHandle.class) {
                if (instance == null) {
                    instance = new NimClientHandle();
                }
            }
        }
        return instance;
    }

    private NimClientHandle() {
        initApi();
    }

    private void initApi() {
        mOkHttpClient = new OkHttpClient();
    }

    public void register(String account, String token, String name, final OnRegisterListener listener) {

        final RequestBody body = new FormBody.Builder()
                  .add("accid", account)
                  .add("token", token)
                  .add("name", name)
                  .build();

        Request request = new Request.Builder()
                  .url(APP_SERVER_BASE_URL + mAppServerUserCreate)
                  .headers(createHeaders())
                  .post(body)
                  .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFailed(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (listener != null) {
                    if (response.code() == 200) {
                        Gson gson = new Gson();
                        RegisterResp resp = gson.fromJson(response.body().string(),
                                  new TypeToken<RegisterResp>(){}.getType());
                        if (resp == null){
                            listener.onFailed("解析返回数据失败" + response.body().string());
                            return;
                        }
                        if (resp.getCode() != 200){
                            listener.onFailed(resp.getDesc());
                            return;
                        }
                        listener.onSuccess();
                    } else {
                        listener.onFailed(response.message());
                    }
                }
            }
        });
    }

    public void updateToken(String account, String pass, final OnRegisterListener listener){
        final RequestBody body = new FormBody.Builder()
                  .add("accid", account)
                  .add("token", pass)

                  .build();

        Request request = new Request.Builder()
                  .url(APP_SERVER_BASE_URL + mAppServerUserUpdate)
                  .headers(createHeaders())
                  .post(body)
                  .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFailed(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (listener != null) {
                    if (response.code() == 200) {
                        Gson gson = new Gson();
                        RegisterResp resp = gson.fromJson(response.body().string(),
                                  new TypeToken<RegisterResp>(){}.getType());
                        if (resp == null){
                            listener.onFailed("解析返回数据失败" + response.body().string());
                            return;
                        }
                        if (resp.getCode() != 200){
                            listener.onFailed(resp.getDesc());
                            return;
                        }
                        listener.onSuccess();
                    } else {
                        listener.onFailed(response.message());
                    }
                }
            }
        });
    }


    /**
     * 生成访问 NIM  APP-SERVICE 所要求的 HEADER
     *
     * @return headers ,in OK HTTP3
     */
    private Headers createHeaders() {
        String nonce = CheckSumUtils.getNonce();
        String time = String.valueOf(System.currentTimeMillis() / 1000L);
        return new Headers.Builder()
                  .add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                  .add("AppKey", AppConst.APP_KEY)
                  .add("Nonce", nonce)
                  .add("CurTime", time)
                  .add("CheckSum", CheckSumUtils.getCheckSum(AppConst.APP_SECURY, nonce, time))
                  .build();
    }
}