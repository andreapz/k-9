package com.tiscali.appmail;

import javax.inject.Singleton;

import com.tiscali.appmail.analytics.LogManager;

import android.app.Application;

import dagger.Module;
import dagger.Provides;

/**
 * Created by andreaputzu on 01/12/16.
 */

@Module
public class ApplicationModule {
    private Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    LogManager provideLogManager() {
        return new LogManager(mApplication);
    }
}
