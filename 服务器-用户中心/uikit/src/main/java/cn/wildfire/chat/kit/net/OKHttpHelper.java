/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.net;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.net.base.ResultWrapper;
import cn.wildfire.chat.kit.net.base.StatusResult;
import cn.wildfire.chat.kit.redpacket.RedPacketActivity;
import cn.wildfire.chat.kit.redpacket.RedPacketInfo;
import cn.wildfire.chat.kit.utils.SPUtils;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by imndx on 2017/12/15.
 */

public class OKHttpHelper {
    private static final String WFC_OKHTTP_COOKIE_CONFIG = "WFC_OK_HTTP_COOKIES";
    private static final Map<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

    private static WeakReference<Context>  AppContext;
    public static void init(Context context) {
        AppContext = new WeakReference<>(context);
    }
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .sslSocketFactory(OkHttpUtil.getIgnoreInitedSslContext().getSocketFactory(), OkHttpUtil.IGNORE_SSL_TRUST_MANAGER_X509)
            .hostnameVerifier(OkHttpUtil.getIgnoreSslHostnameVerifier())
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);
                    if (AppContext != null && AppContext.get() != null) {
                        SharedPreferences sp = AppContext.get().getSharedPreferences(WFC_OKHTTP_COOKIE_CONFIG, 0);
                        Set<String>  set = new HashSet<>();
                        for (Cookie k:cookies) {
                            set.add(gson.toJson(k));
                        }
                        sp.edit().putStringSet(url.host(), set).apply();
                    }
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    if (cookies == null) {
                        if (AppContext != null && AppContext.get() != null) {
                            SharedPreferences sp = AppContext.get().getSharedPreferences(WFC_OKHTTP_COOKIE_CONFIG, 0);
                            Set<String>  set = sp.getStringSet(url.host(), new HashSet<>());
                            cookies = new ArrayList<>();
                            for (String s:set) {
                                Cookie cookie = gson.fromJson(s, Cookie.class);
                                cookies.add(cookie);
                            }
                            cookieStore.put(url.host(), cookies);
                        }
                    }

                    return cookies;
                }
            })
            .build();

    private static Gson gson = new Gson();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static class OkHttpUtil {
        public static X509TrustManager IGNORE_SSL_TRUST_MANAGER_X509 = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }
        };
        public static SSLContext getIgnoreInitedSslContext() {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{IGNORE_SSL_TRUST_MANAGER_X509}, new SecureRandom());
                return sslContext;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        public static HostnameVerifier getIgnoreSslHostnameVerifier() {
            return new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            };
        }
    }

    public static <T> void get(final String url, Map<String, String> params, final Callback<T> callback) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (params != null) {
            HttpUrl.Builder builder = httpUrl.newBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
            httpUrl = builder.build();
        }

        final Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(-1, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(url, call, response, callback);
            }
        });

    }

    public static <T> void post(final String url, Map<String, Object> param, final Callback<T> callback) {
        RequestBody body = RequestBody.create(JSON, gson.toJson(param));
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(-1, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(url, call, response, callback);

            }
        });
    }

    public static <T> void postJson(final String url, String json, final Callback<T> callback) {
        if (url == null) {
            return;
        }
        if (url.equalsIgnoreCase("")) {
            return;
        }
        if (url.length() < 10) {
            return;
        }
        String beginString = url.substring(0, 4);
        if (!beginString.equalsIgnoreCase("http")) {
            return;
        }
        RequestBody body = RequestBody.create(JSON, json);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(-1, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(url, call, response, callback);

            }
        });
    }

    public static <T> void getResult(String transferId,String finalText, String token, final Callback<T> callback){

        new Thread(new Runnable() {
            @Override
            public void run() {

                boolean ret = false;
                for(int i=0;i<8;i++){
                    try {
                        Thread.sleep(2000);
                        ret = getResult2(transferId,finalText, token, callback);
                        if (ret) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (!ret) {
                    if (callback != null)
                        callback.onFailure(-1, "time out");
                }
            }
        }).start();

        /*OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(BuildConfig.GETTRANSFEREST_URL+"?messageId="+transferId)
                .get()
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);

        //同步
        try {
            Response response = call.clone().execute();
            if(response.isSuccessful()){
                call.enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {


                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String result = response.body().string();
                        callback.onSuccess1(result);
                        *//*System.out.println("@@@   result="+result);
                        if(result == null || result.equals("")){
                            return;
                        }
                        Gson gson = new Gson();
                        RedPacketInfo redPacketInfo = gson.fromJson(result,RedPacketInfo.class);
                        if(redPacketInfo.getCode() == 200){
                                *//**//*String txid = json.getString("txid");
                            String urlQuery = json.getString("queryUrl");
                            String urlFetch = json.getString("url");*//**//*

                            String txid = redPacketInfo.getData().getTxid();
                            String urlQuery = redPacketInfo.getData().getQueryUrl();
                            String urlFetch = redPacketInfo.getData().getUrl();

                            JSONObject data = new JSONObject();
                            if(urlQuery == null)
                                urlQuery = "";
                            if(urlFetch == null)
                                urlFetch = "";
                            data.put("txid", txid);
                            data.put("text", finalText);
                            data.put("packetID", txid);
                            data.put("unpackID", txid);
                            data.put("urlQuery", urlQuery);
                            data.put("urlFetch", urlFetch);
                            //data.put("dapp", "OSNS6qJXyTT3KrNVngopdCHwG1tv87z3fQB2txtLTEKTW2dS3N6");
                            Intent intent = new Intent();
                            intent.putExtra("id", txid);
                            intent.putExtra("info", data.toString());
                            intent.putExtra("text", finalText);
                            setResult(Activity.RESULT_OK, intent);
                        }*//*

                    }
                });

            }else{

            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    private static <T> boolean getResult2(String transferId,String finalText ,String token,  final Callback<T> callback2){
        String url_GETTRANSFEREST_URL = (String) SPUtils.get(AppContext.get(),"GETTRANSFEREST_URL","");
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url_GETTRANSFEREST_URL+"?messageId="+transferId)
                .get()
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);

        //同步
        try {
            Response response = call.clone().execute();
            if(response.isSuccessful()){
                call.enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        callback2.onFailure(-1, e.getMessage());

                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String result = response.body().string();
                        if(TextUtils.isEmpty(result)){
                            callback2.onFailure(-1, String.valueOf(R.string.service_error));
                            return;
                        }
                        JSONObject resultJson = JSONObject.parseObject(result);
                        if(resultJson == null){
                            callback2.onFailure(-1, String.valueOf(R.string.service_error));
                            return;
                        }
                        int code = resultJson.getIntValue("code");
                        if (code == 200) {
                            callback2.onSuccess1(result);
                        } else if (code == 40000004) {
                            String msg = resultJson.getString("msg");
                            JSONObject resultJson2 = JSONObject.parseObject(msg);
                            String msg2 = resultJson2.getString("error_count");
                            callback2.onFailure(code, msg2);
                        } else {
                            String msg = resultJson.getString("msg");
                            callback2.onFailure(code, msg);
                        }


                        //System.out.println("@@@   result="+result);

                        /*if(result == null || result.equals("")){
                            return;
                        }
                        Gson gson = new Gson();
                        RedPacketInfo redPacketInfo = gson.fromJson(result,RedPacketInfo.class);
                        if(redPacketInfo.getCode() == 200){
                                *//*String txid = json.getString("txid");
                            String urlQuery = json.getString("queryUrl");
                            String urlFetch = json.getString("url");*//*

                            String txid = redPacketInfo.getData().getTxid();
                            String urlQuery = redPacketInfo.getData().getQueryUrl();
                            String urlFetch = redPacketInfo.getData().getUrl();
                            if(txid == null){
                                Toast.makeText(RedPacketActivity.this, R.string.server_parameter_error, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONObject data = new JSONObject();
                            if(urlQuery == null)
                                urlQuery = "";
                            if(urlFetch == null)
                                urlFetch = "";
                            data.put("txid", txid);
                            data.put("text", finalText);
                            data.put("packetID", txid);
                            data.put("unpackID", txid);
                            data.put("urlQuery", urlQuery);
                            data.put("urlFetch", urlFetch);
                            //data.put("dapp", "OSNS6qJXyTT3KrNVngopdCHwG1tv87z3fQB2txtLTEKTW2dS3N6");
                            Intent intent = new Intent();
                            intent.putExtra("id", txid);
                            intent.putExtra("info", data.toString());
                            intent.putExtra("text", finalText);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }*/

                    }
                });
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }




    public static <T> void postJsonWithToken(final String url, String json, final Callback<T> callback) {
        SharedPreferences sp = AppContext.get().getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        System.out.println("@@@              token=  "+token);
        RequestBody body = RequestBody.create(JSON, json);
        final Request request = new Request.Builder()
                .addHeader("X-TOKEN", token)
                .url(url)
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(-1, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(url, call, response, callback);

            }
        });
    }
    public static <T> void postJsonNoToken(final String url, String json, final Callback<T> callback) {
        SharedPreferences sp = AppContext.get().getSharedPreferences("config", Context.MODE_PRIVATE);
        //String token = sp.getString("token", "");
        //System.out.println("@@@              token=  "+token);
        RequestBody body = RequestBody.create(JSON, json);
        final Request request = new Request.Builder()
                //.addHeader("X-TOKEN", token)
                .url(url)
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(-1, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(url, call, response, callback);

            }
        });
    }



    public static <T> void put(final String url, Map<String, String> param, final Callback<T> callback) {
        RequestBody body = RequestBody.create(JSON, gson.toJson(param));
        final Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(-1, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(url, call, response, callback);

            }
        });
    }

    public static <T> void upload(String url, Map<String, String> params, File file, MediaType mediaType, final Callback<T> callback) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(mediaType, file));

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(-1, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(url, call, response, callback);
            }
        });
    }

    public static void clearCookies(){
        SharedPreferences sp = AppContext.get().getSharedPreferences(WFC_OKHTTP_COOKIE_CONFIG, 0);
        sp.edit().clear().apply();
        cookieStore.clear();
    }

    private static <T> void handleResponse(String url, Call call, Response response, Callback<T> callback) {
        if (callback != null) {
            if (!response.isSuccessful()) {
                callback.onFailure(response.code(), response.message());
                return;
            }

            Type type;
            if (callback instanceof SimpleCallback) {
                Type types = callback.getClass().getGenericSuperclass();
                type = ((ParameterizedType) types).getActualTypeArguments()[0];
            } else {
                Type[] types = callback.getClass().getGenericInterfaces();
                type = ((ParameterizedType) types[0]).getActualTypeArguments()[0];
            }

            if (type.equals(Void.class)) {
                callback.onSuccess((T) null);
                return;
            }

            if (type.equals(String.class)) {
                try {
                    callback.onSuccess((T) response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }


            try {
                StatusResult statusResult;
                if (type instanceof Class && type.equals(StatusResult.class)) {
                    statusResult = gson.fromJson(response.body().string(), StatusResult.class);
                    if (statusResult.isSuccess()) {
                        callback.onSuccess((T) statusResult);
                    } else {
                        callback.onFailure(statusResult.getCode(), statusResult.getMessage());
                    }
                } else {
                    ResultWrapper<T> wrapper = gson.fromJson(response.body().string(), new ResultType(type));
                    if (wrapper == null) {
                        callback.onFailure(-1, "response is null");
                        return;
                    }
                    if (wrapper.isSuccess() && wrapper.getResult() != null) {
                        callback.onSuccess(wrapper.getResult());
                    } else {
                        callback.onFailure(wrapper.getCode(), wrapper.getMessage());
                    }
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                callback.onFailure(-1, e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                callback.onFailure(-1, e.getMessage());
            }
        }
    }

    private static class ResultType implements ParameterizedType {
        private final Type type;

        public ResultType(Type type) {
            this.type = type;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{type};
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public Type getRawType() {
            return ResultWrapper.class;
        }
    }
}
