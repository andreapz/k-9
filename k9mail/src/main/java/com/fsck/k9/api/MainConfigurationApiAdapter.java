package com.fsck.k9.api;

import com.fsck.k9.api.model.Config;
import com.fsck.k9.api.model.MainConfig;

import java.util.concurrent.Executor;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by andreaputzu on 22/12/16.
 */

public class MainConfigurationApiAdapter {

    private static HttpLoggingInterceptor mInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    private static OkHttpClient mClient = new OkHttpClient.Builder().addInterceptor(mInterceptor).build();

    private static Retrofit mRetrofit = new Retrofit.Builder()
            .baseUrl("https://tiscaliapp-api.tiscali.it/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .client(mClient)
//            .callbackExecutor(new Executor() {
//                @Override
//                public void execute(Runnable runnable) {
//                    runnable.run();
//                }
//            })
            .build();


    public static Observable<MainConfig> getConfig() {
        return mRetrofit.create(MainConfigurationApiClient.class).getConfig();
    }
}
