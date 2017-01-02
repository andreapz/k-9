package com.fsck.k9.api;

import android.util.Log;

import com.fsck.k9.api.model.Config;
import com.fsck.k9.api.model.MainConfig;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by andreaputzu on 02/01/17.
 */

public class MainConfigurationTest {

    @Test
    public void getConfig() throws Exception {

        Observable<MainConfig> mainconfig = MainConfigurationApiAdapter.getConfig();

        Assert.assertNotNull(mainconfig);

        mainconfig
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MainConfig>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Assert.fail();
                    }

                    @Override
                    public void onNext(MainConfig mainConfig) {
                        Assert.assertNotNull(mainConfig.getConfig().getAge());
                        Log.i("TEST",""+mainConfig.getConfig().getAge());
                    }
                });

    }
}
