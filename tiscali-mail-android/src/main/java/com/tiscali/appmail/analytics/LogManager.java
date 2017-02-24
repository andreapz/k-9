package com.tiscali.appmail.analytics;

import javax.inject.Singleton;

import com.tiscali.appmail.BuildConfig;
import com.tiscali.appmail.R;
import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Webtrekk;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by andreaputzu on 17/02/17.
 */
@Singleton
public class LogManager {

    private final Application mApplication;
    private Webtrekk mWebtrekk;
    private TrackingParameter mTp;
    private boolean mStart = false;

    public LogManager(Application application) {
        mApplication = application;
    }


    public void init() {
        mWebtrekk = Webtrekk.getInstance();
        mWebtrekk.initWebtrekk(mApplication);

        if (BuildConfig.DEBUG) {
            Webtrekk.setLoggingEnabled(true);
        }

        String version = "";
        PackageManager manager = mApplication.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(mApplication.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String[] entries = mApplication.getResources()
                .getStringArray(R.array.webtrekk_adv_parameters_entries_tiscali);
        String[] values = mApplication.getResources()
                .getStringArray(R.array.webtrekk_adv_parameters_values_tiscali);

        mTp = new TrackingParameter();

        for (int i = 0; i < values.length; i++) {
            mTp.add(TrackingParameter.Parameter.PAGE, entries[i], values[i]);
        }

        mTp.add(TrackingParameter.Parameter.PAGE, entries[entries.length - 1], version);

        mStart = true;
    }

    public void track(int value) {
        String page = mApplication.getResources().getString(value);
        track(page);
    }

    public void track(String value) {
        if (!mStart) {
            init();
        }
        mWebtrekk.setCustomPageName(value);
        mWebtrekk.track(mTp);
    }
}
