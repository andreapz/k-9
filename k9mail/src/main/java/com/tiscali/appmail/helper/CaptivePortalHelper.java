package com.tiscali.appmail.helper;

/**
 * Created by thomascastangia on 02/02/17.
 */

import com.tiscali.appmail.activity.CaptivPortalActivity;
import com.tiscali.appmail.fragment.CaptivePortalWebViewFragment;

import android.content.Context;
import android.text.format.DateUtils;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;


public class CaptivePortalHelper {

    private static final long LOGIN_SHOW_WEBVIEV_GUARD_TIME = 2 * DateUtils.MINUTE_IN_MILLIS;

    private static CaptivePortalHelper sInstance;
    private final Context mContext;

    public static CaptivePortalHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CaptivePortalHelper(context);
        }
        return sInstance;
    }


    private String mLastWebViewShownNetworkName = null;
    private long mLastWebViewShownTime = 0;


    private CaptivePortalHelper(Context context) {
        mContext = context;
    }

    /**
     * Test connectivity against captive portal presence using data passed on the main configuration
     * if available.
     **/
    public boolean isCaptivePortalConnection() {


        String http204NoContentUrl = null;
        http204NoContentUrl = CaptivePortalWebViewFragment.CAPTIVE_PORTAL_URL;
        return ConnectivityUtils.isCaptivePortalConnection(http204NoContentUrl);


    }


    /**
     * Show the login web view, avoiding hitting it for every call, guard time is 2 minutes
     */
    public void showLoginWebView() {
        showLoginWebView(false);
    }


    /**
     * Show the login web view, avoiding hitting it for every call, guard time is 2 minutes
     *
     * @param forced force showing the WebView
     */
    public void showLoginWebView(boolean forced) {


        long now = System.currentTimeMillis();
        String network = NetworkHelper.getInstance(mContext).getCurrentNetworkName();

        if (!forced && (mLastWebViewShownNetworkName != null)
                && mLastWebViewShownNetworkName.equals(network)) {
            // asking to show the web view for the same network

            if (now < (mLastWebViewShownTime + LOGIN_SHOW_WEBVIEV_GUARD_TIME)) {
                // inside the guard time
                return; // skip
            }
        }

        mLastWebViewShownTime = now;
        mLastWebViewShownNetworkName = network;
        Observable.empty().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        CaptivPortalActivity.startActivity(mContext);

                    }

                    @Override
                    public void onError(Throwable e) {

            }

                    @Override
                    public void onNext(Object o) {

            }
                });

    }


    public void setLoginWebViewAsClosed() {
        // avoid long time showing activity to consume the guard time
        long now = System.currentTimeMillis();
        mLastWebViewShownTime = now;
    }


}
