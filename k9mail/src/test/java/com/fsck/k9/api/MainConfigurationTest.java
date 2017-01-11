package com.fsck.k9.api;

import android.content.Context;
import android.util.Log;

import com.fsck.k9.Account;
import com.fsck.k9.Preferences;
import com.fsck.k9.activity.ActivityListener;
import com.fsck.k9.api.model.Authorize;
import com.fsck.k9.api.model.MainConfig;
import com.fsck.k9.api.model.UserLogin;
import com.fsck.k9.mail.Message;
import com.fsck.k9.preferences.Storage;
import com.fsck.k9.preferences.StorageEditor;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;


import rx.Scheduler;
import rx.Subscriber;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.schedulers.Schedulers;

import static org.mockito.Mockito.mock;


/**
 * Created by andreaputzu on 02/01/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 21)
public class MainConfigurationTest extends RxAndroidSchedulersHook {

    @Override
    public Scheduler getMainThreadScheduler() {
        return Schedulers.immediate();
    }

    @Test
    public void getConfigTest() throws Exception {

//        ApiController.getConfig(new Subscriber<MainConfig>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Assert.fail();
//                    }
//
//                    @Override
//                    public void onNext(MainConfig mainConfig) {
//                        Assert.assertNotNull(mainConfig.getConfig().getAge());
//                        Log.i("TEST", "" + mainConfig.getConfig().getAge());
//                    }
//                });

    }

    @Test
    public void getAuthorizeTest() throws Exception {

//        ApiController.getAuthorize(new Subscriber<Authorize>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Assert.fail();
//                    }
//
//                    @Override
//                    public void onNext(Authorize authorize) {
//                        Log.i("APITEST","Result: "+ authorize.getResult());
//
//                    }
//                });
    }

    @Test
    public void postUserLoginTest() throws Exception {

//        ApiController.postUserLogin(new Subscriber<UserLogin>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Assert.fail();
//                    }
//
//                    @Override
//                    public void onNext(UserLogin userLogin) {
//                        Log.i("APITEST","Username: "+userLogin.getUser().getAccount());
//                    }
//                });
    }

    private Context context;
    private Preferences pref;
    private Storage storage;
    private StorageEditor editor;

    @Before
    public void before() {
        context = RuntimeEnvironment.application;
        pref = Preferences.getPreferences(context);
        storage = pref.getStorage();
        editor = storage.edit();
    }

    @Test
    public void preferenceBooleanTest() throws Exception {
        String key = "TestBoolKey";

        boolean test = true;
        editor.putBoolean(key, test);
        editor.commit();
        boolean value = storage.getBoolean(key, false);
        Assert.assertEquals(test, value);

        test = false;
        editor.putBoolean(key, test);
        editor.commit();
        value = storage.getBoolean(key, false);
        Assert.assertEquals(test, value);

        editor.remove(key);
        editor.commit();
    }

    @Test
    public void preferenceIntTest() throws Exception {
        String key = "TestIntKey";

        int test = 1;
        editor.putInt(key, test);
        editor.commit();
        int value = storage.getInt(key, 0);
        Assert.assertEquals(test, value);

        test = 2;
        editor.putInt(key, test);
        editor.commit();
        value = storage.getInt(key, 0);
        Assert.assertEquals(test, value);

        editor.remove(key);
        editor.commit();
    }
}
