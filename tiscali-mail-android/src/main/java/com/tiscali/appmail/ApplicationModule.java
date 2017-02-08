package com.tiscali.appmail;

import javax.inject.Singleton;

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
}
