package com.fsck.k9.activity;

import com.fsck.k9.api.ApiController;
import com.fsck.k9.fragment.MailPresenter;
import com.fsck.k9.fragment.MediaPresenter;
import com.fsck.k9.fragment.NewsPresenter;
import com.fsck.k9.fragment.OffersPresenter;
import com.fsck.k9.fragment.VideoPresenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

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

    @Provides
    @ActivityScope
    LayoutInflater provideLayoutInflater() {
        System.out.println("Provide LayoutInflater");
        return mActivity.getLayoutInflater();
    }

    @Provides
    @ActivityScope
    MailPresenter provideMailPresenter() {
        return new MailPresenter((INavigationDrawerActivityListener) mActivity, mIntent);
    }

    @Provides
    @ActivityScope
    ApiController provideApiController() {
        return new ApiController(mActivity);
    }

    @Provides
    @ActivityScope
    NewsPresenter provideNewsPresenter() {
        return new NewsPresenter((INavigationDrawerActivityListener) mActivity, mIntent,
                MediaPresenter.Type.NEWS);
    }

    @Provides
    @ActivityScope
    VideoPresenter provideVideoPresenter() {
        return new VideoPresenter((INavigationDrawerActivityListener) mActivity, mIntent,
                MediaPresenter.Type.VIDEO);
    }

    @Provides
    @ActivityScope
    OffersPresenter provideOffersPresenter() {
        return new OffersPresenter((INavigationDrawerActivityListener) mActivity, mIntent,
                MediaPresenter.Type.OFFERS);
    }

    @Provides
    @ActivityScope
    Context provideContext() {
        return mActivity;
    }

    @Provides
    @ActivityScope
    Intent provideIntent() {
        return mIntent;
    }
}

