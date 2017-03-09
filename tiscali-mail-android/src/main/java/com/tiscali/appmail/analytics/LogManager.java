package com.tiscali.appmail.analytics;

import javax.inject.Singleton;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
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

    private static final String TRACK_VIEW = "VIEW";
    private static final String TRACK_VIEW_ID = "VIEW_ID";
    private static final String TRACK_URL = "URL";
    private static final String TRACK_URL_ID = "URL_ID";
    private static final String TRACK_VERSION = "VERSION";


    private final Application mApplication;
    private Webtrekk mWebtrekk;
    private TrackingParameter mTp;
    private boolean mStart = false;
    private String mVersion;

    public LogManager(Application application) {
        mApplication = application;
    }


    public void init() {
        mWebtrekk = Webtrekk.getInstance();
        mWebtrekk.initWebtrekk(mApplication);

        if (BuildConfig.DEBUG) {
            Webtrekk.setLoggingEnabled(true);
        }

        mVersion = "";
        PackageManager manager = mApplication.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(mApplication.getPackageName(), 0);
            mVersion = info.versionName;
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

        mTp.add(TrackingParameter.Parameter.PAGE, entries[entries.length - 1], mVersion);

        mStart = true;
    }

    public void trackView(int value) {
        String page = mApplication.getResources().getString(value);
        trackView(page);
    }

    public void trackView(String value) {
        if (!mStart) {
            init();
        }

        trackWebTrack(value);

        Answers.getInstance()
                .logContentView(new ContentViewEvent().putContentName(value)
                        .putContentType(TRACK_VIEW).putContentId(TRACK_VIEW_ID)
                        .putCustomAttribute(TRACK_VERSION, mVersion));
    }

    private void trackWebTrack(String value) {
        mWebtrekk.setCustomPageName(value);
        mWebtrekk.track(mTp);
    }

    public void trackUrl(String url) {
        if (!mStart) {
            init();
        }

        trackWebTrack(url);

        Answers.getInstance()
                .logContentView(new ContentViewEvent().putContentName(url).putContentType(TRACK_URL)
                        .putContentId(TRACK_URL_ID).putCustomAttribute(TRACK_VERSION, mVersion));
    }
}
