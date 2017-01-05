package com.fsck.k9.api;

import android.util.Log;

import com.fsck.k9.api.model.Authorize;
import com.fsck.k9.api.model.MainConfig;
import com.fsck.k9.api.model.UserLogin;
import com.fsck.k9.error.RxErrorHandlingCallAdapterFactory;

import java.io.IOException;
import java.util.concurrent.Executor;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by andreaputzu on 22/12/16.
 */

public class ApiController {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_AUTHORIZED = "Authorized";
    public static final int RETRY_COUNT = 1;

    private static String mAuthorizedHeaderValue = "";

    private static ResponseHeaderInterceptor.ResponseHeaderListener headerListener = new ResponseHeaderInterceptor.ResponseHeaderListener(){
        @Override
        public void onHeadersIntercepted(Headers headers){
            String headerAuthValue = headers.get(HEADER_AUTHORIZED);
            if(headerAuthValue != null) {
                mAuthorizedHeaderValue = headerAuthValue;
            }
        }
    };

    private static HttpLoggingInterceptor mLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    public static final String HEADER_APP_ID = "app_id";
    private static Interceptor mInterceptor = new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header(HEADER_AUTHORIZATION, mAuthorizedHeaderValue+","+HEADER_APP_ID+"=\"123456\"");

            Request request = requestBuilder.build();
//            return chain.proceed(request);
            Response response = chain.proceed(request);

//            response.code();
//
//            int tryCount = 0;
//            if(!response.isSuccessful() && tryCount++ < 3) {
//                Log.d("intercept", "Request is not successful - " + request.method()+"@"+request.url()+" "+tryCount);
//                response = chain.proceed(request);
//            }

            return response;
        }
    };

    private static OkHttpClient mAuthorizeClient = new OkHttpClient.Builder()
            .addInterceptor(mLoggingInterceptor)
            .addInterceptor(mInterceptor)
            .addInterceptor(new ResponseHeaderInterceptor(headerListener))
            .build();

    private static Retrofit mAuthorizeRetrofit = new Retrofit.Builder()
            .baseUrl("https://tiscaliapp-api.tiscali.it/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .client(mAuthorizeClient)
            .callbackExecutor(new Executor() {
                @Override
                public void execute(Runnable runnable) {
                    runnable.run();
                }
            })
            .build();

    private static OkHttpClient mClient = new OkHttpClient.Builder()
            .addInterceptor(mLoggingInterceptor)
            .addInterceptor(mInterceptor)
            .build();

    private static Retrofit mApiRetrofit = new Retrofit.Builder()
            .baseUrl("https://tiscaliapp-api.tiscali.it/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
            .client(mClient)
            .callbackExecutor(new Executor() {
                @Override
                public void execute(Runnable runnable) {
                    runnable.run();
                }
            })
            .build();


    public static class ResponseHeaderInterceptor implements Interceptor {
        public interface ResponseHeaderListener {
            public void onHeadersIntercepted(Headers headers);
        }

        private ResponseHeaderListener mListener;

        public ResponseHeaderInterceptor() {
        }

        ;

        public ResponseHeaderInterceptor(ResponseHeaderListener listener) {
            mListener = listener;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            if (mListener != null) {
                mListener.onHeadersIntercepted(response.headers());
            }
            return response;
        }
    }

    private static ApiClient createClient() {
        return mApiRetrofit.create(ApiClient.class);
    }

    public static void getConfig(Subscriber<MainConfig> subscriber) {
        createClient()
                .getConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT)
                .subscribe(subscriber);
    }

    public static Observable<Authorize> getAuthorize() {
        return mAuthorizeRetrofit
                .create(ApiClient.class)
                .getAuthorize("udid","123456")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }

    public static Observable<UserLogin> postUserLogin() {
        return createClient()
                .postUserLogin("gigya_dev1@tiscali.it", "Gigya21")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }

    public static Observable<UserLogin> getMe() {
        return createClient()
                .getMe()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }
}

