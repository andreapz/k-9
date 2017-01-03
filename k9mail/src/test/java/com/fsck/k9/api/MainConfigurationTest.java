package com.fsck.k9.api;

import android.util.Log;

import com.fsck.k9.api.model.Authorize;
import com.fsck.k9.api.model.MainConfig;
import com.fsck.k9.api.model.UserLogin;

import junit.framework.Assert;

import org.junit.Test;


import rx.Scheduler;
import rx.Subscriber;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.schedulers.Schedulers;


/**
 * Created by andreaputzu on 02/01/17.
 */

public class MainConfigurationTest extends RxAndroidSchedulersHook {

    @Override
    public Scheduler getMainThreadScheduler() {
        return Schedulers.immediate();
    }

    @Test
    public void getConfigTest() throws Exception {

        ApiController.getConfig(new Subscriber<MainConfig>() {
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
                        Log.i("TEST", "" + mainConfig.getConfig().getAge());
                    }
                });

    }

    @Test
    public void getAuthorizeTest() throws Exception {

        ApiController.getAuthorize(new Subscriber<Authorize>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Assert.fail();
                    }

                    @Override
                    public void onNext(Authorize authorize) {
                        Log.i("APITEST","Result: "+ authorize.getResult());

                    }
                });
    }

    @Test
    public void postUserLoginTest() throws Exception {

        ApiController.postUserLogin(new Subscriber<UserLogin>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Assert.fail();
                    }

                    @Override
                    public void onNext(UserLogin userLogin) {
                        Log.i("APITEST","Username: "+userLogin.getUser().getAccount());
                    }
                });
    }
}
