package com.fsck.k9.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

import com.fsck.k9.fragment.MailPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by andreaputzu on 01/12/16.
 */

@Module
public class ActivityModule {
    private Activity mActivity;
    private Intent mIntent;

    public ActivityModule(Activity activity, Intent intent) {
        mActivity = activity;
        mIntent = intent;
    }

    @Provides
    Activity provideActivity() {
        return mActivity;
    }

    @Provides @ActivityScope
    LayoutInflater provideLayoutInflater() {
        System.out.println("Provide LayoutInflater");
        return mActivity.getLayoutInflater();
    }

    @Provides @ActivityScope
    MailPresenter provideMailPresenter() {
        return new MailPresenter(mActivity, mIntent);
    }

    @Provides @ActivityScope
    Context provideContext() {
        return mActivity;
    }

    @Provides @ActivityScope
    Intent provideIntent() {
        return mIntent;
    }
}
