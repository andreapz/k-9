package com.fsck.k9.api;

import com.fsck.k9.api.model.Config;

import java.util.concurrent.Executor;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by andreaputzu on 22/12/16.
 */

public class MainConfigurationApiAdapter {

    private static Retrofit mRetrofit = new Retrofit.Builder()
            .baseUrl("https://tiscaliapp-api.tiscali.it/1/config/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .callbackExecutor(new Executor() {
                @Override
                public void execute(Runnable runnable) {
                    runnable.run();
                }
            })
            .build();


    public static Observable<Config> getConfig() {
        return mRetrofit.create(MainConfigurationApiClient.class).getConfig();
    }
}
