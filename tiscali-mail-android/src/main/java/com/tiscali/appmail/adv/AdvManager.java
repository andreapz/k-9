package com.tiscali.appmail.adv;

import java.util.HashMap;
import java.util.Map;

import com.dotandmedia.android.sdk.AdConfigListener;
import com.dotandmedia.android.sdk.AdListener;
import com.dotandmedia.android.sdk.AdSizeConfig;
import com.dotandmedia.android.sdk.AdView;
import com.dotandmedia.android.sdk.DotAndMediaSDK;
import com.dotandmedia.android.sdk.mraid.BannerSizeProperties;
import com.tiscali.appmail.Account;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.R;
import com.tiscali.appmail.activity.NavigationDrawerActivity;
import com.tiscali.appmail.activity.TiscaliUtility;
import com.tiscali.appmail.api.model.Me;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by andreaputzu on 01/03/17.
 */

public class AdvManager {

    // MPT BANNER 320x50
    private static final float BANNER_WIDTH_SMARTPHONE_HEAD = 320.0f;
    private static final float BANNER_HEIGHT_SMARTPHONE_HEAD = 50.0f;

    // MPT BANNER 768x90
    private static final float BANNER_WIDTH_TABLET_PORTRAIT = 768.0f;
    private static final float BANNER_HEIGHT_TABLET_PORTRAIT = 90.0f;

    // MPT BANNER 1024x90
    private static final float BANNER_WIDTH_TABLET_LANDSCAPE = 1024.0f;
    private static final float BANNER_HEIGHT_TABLET_LANDSCAPE = 90.0f;
    private final Activity mActivity;
    private LinearLayout mLinearLayout;


    public AdvManager(Activity activity) {
        mActivity = activity;
    }

    /**
     * Remove all DotAndAd AdViews.
     * <p>
     * <p>
     * This will remove all DotAndAd AdViews.
     * </p>
     */
    public void removeAdView() {
        // if (mAdViewToAdd != null) {
        // mAdViewToAdd.removeAdListener();
        // }
        if (mLinearLayout != null) {
            mLinearLayout.removeAllViews();
        }
        Log.i("APZ", "adv removeAllViews");
    }

    /**
     * Add a DotAndAd AdView.
     * <p>
     * <p>
     * This will add a DotAndAd AdView.
     * </p>
     *
     * @param account Current account
     */
    private void addAdView(final Account account) {

        boolean isActiveDotAndAdAdv = true; // Boolean.parseBoolean(TiscaliConfigRemote.getPreferenceStringByKey(context,
        // TiscaliDotAndAd.TAG_DOTANDAD_ADV));

        // if (// !Utility.hasConnectivity(context) ||
        // !isActiveDotAndAdAdv
        // // || (noAdvPreApiLevel != null && Build.VERSION.SDK_INT <
        // // Integer.parseInt(noAdvPreApiLevel))
        // ) {
        //
        // return;
        // }

        boolean isTabletDevice = TiscaliUtility.isTablet(mActivity);
        boolean isPortraitOrientation = mActivity.getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? true
                        : false;

        String mpo = !isTabletDevice ? mActivity.getString(R.string.dotandad_adv_mpo_smartphone)
                : mActivity.getString(R.string.dotandad_adv_mpo_tablet);
        String mpt =
                !isTabletDevice ? mActivity.getString(R.string.dotandad_adv_mpt_smartphone_head)
                        : (isPortraitOrientation
                                ? mActivity.getString(R.string.dotandad_adv_mpt_tablet_portrait)
                                : mActivity.getString(R.string.dotandad_adv_mpt_tablet_landscape));

        // CID: Customer ID - MPO: Multipoint - MPT: Mediapoint
        AdView adViewToAdd = new AdView(mActivity, // mActivity.getString(R.string.dotandad_adv_cid),
                                                   // mpo,
                mpt, null); // Color.TRANSPARENT

        Log.i("APZ", "adv params mpo:" + mpo + " " + " mpt:" + mpt);

        final LinearLayout linearLayoutAdvContainer = mLinearLayout;

        adViewToAdd.setAdListener(getAdListener(adViewToAdd, linearLayoutAdvContainer));

        /*
         * BANNER SMARTPHONE HEAD 320x50 BANNER TABLET PORTRAIT 768x90 BANNER TABLET LANDSCAPE
         * 1024x90
         */
        final float originalBannerWidth = !isTabletDevice ? BANNER_WIDTH_SMARTPHONE_HEAD
                : (isPortraitOrientation ? BANNER_WIDTH_TABLET_PORTRAIT
                        : BANNER_WIDTH_TABLET_LANDSCAPE);
        final float originalBannerHeight = !isTabletDevice ? BANNER_HEIGHT_SMARTPHONE_HEAD
                : (isPortraitOrientation ? BANNER_HEIGHT_TABLET_PORTRAIT
                        : BANNER_HEIGHT_TABLET_LANDSCAPE);

        final float formFactor = originalBannerWidth / originalBannerHeight;
        float newBannerWidth = mActivity.getResources().getDisplayMetrics().widthPixels;
        float newBannerHeight = newBannerWidth / formFactor;

        adViewToAdd.setLayoutParams(
                new ViewGroup.LayoutParams((int) newBannerWidth, (int) newBannerHeight));

        Log.i("APZ", "adv layout params w:" + newBannerWidth + " " + " h:" + newBannerHeight);
        // //UTP Profile
        // if (account != null) {
        // try {
        // final String utpAccountData = TiscaliUTP.getAccountData(account, activity);
        //
        // if (!TextUtils.isEmpty(utpAccountData) && utpAccountData.length() > 40) {
        // // The HashMap to pass to the Adview
        // HashMap<String, String> custPar = new HashMap<String, String>();
        //
        // // Pass uvfc custom parameters to the HashMap
        // custPar.put("uvfc", utpAccountData.substring(40));
        //
        // // Set the custom parameters
        // adViewToAdd.setCustomParameters(custPar);
        // }
        // } catch (Exception e) {
        // Log.e(K9.LOG_TAG, "Exception in TiscaliDotAndAd - addAdView - TiscaliUTP: " + e);
        // }
        // }

        // Invalidate the view layout, this will schedule a layout pass of the view tree

        adViewToAdd.requestLayout();

        linearLayoutAdvContainer.addView(adViewToAdd, 0);

        ((NavigationDrawerActivity) mActivity).setMarginVisibility(true);

        Log.i("APZ", "adv added");

        // Observable.empty().observeOn(AndroidSchedulers.mainThread())
        // .subscribe(new Subscriber<Object>() {
        // @Override
        // public void onCompleted() {
        //
        // }
        //
        // @Override
        // public void onError(Throwable e) {
        //
        // }
        //
        // @Override
        // public void onNext(Object o) {
        //
        // }
        // });
    }

    @NonNull
    private AdListener getAdListener(final AdView adViewToAdd,
            final LinearLayout linearLayoutAdvContainer) {
        return new AdListener() {
            @Override
            public void handleRequest(String arg0) {
                Log.i("APZ", "handleRequest " + arg0);
            }

            @Override
            public boolean onAdSkippedByFreq() {
                linearLayoutAdvContainer.removeView(adViewToAdd);
                ((NavigationDrawerActivity) mActivity).setMarginVisibility(false);
                Log.i("APZ", "onAdSkippedByFreq");
                return false;
            }

            @Override
            public void onAdReLoadedByRefresh() {
                Log.i("APZ", "onAdReLoadedByRefresh");
            }

            @Override
            public boolean onClose() {
                Log.i("APZ", "onClose");
                return false;
            }

            @Override
            public void onConfigurationLoaded() {
                Log.i("APZ", "onConfigurationLoaded");
            }

            @Override
            public void onCreateEvent() {
                Log.i("APZ", "onCreateEvent");
            }

            @Override
            public boolean onExpand() {
                Log.i("APZ", "onExpand");
                return false;
            }

            @Override
            public boolean onOpen() {
                Log.i("APZ", "onOpen");
                return false;
            }

            @Override
            public void onOrientationProperties() {
                Log.i("APZ", "onOrientationProperties");
            }

            @Override
            public void onPlayVideo() {
                Log.i("APZ", "onPlayVideo");
            }

            @Override
            public boolean onResize() {
                Log.i("APZ", "onResize");
                return false;
            }

            @Override
            public void onNoAdv() {
                Log.i("APZ", "onNoAdv");
                linearLayoutAdvContainer.removeView(adViewToAdd);
                ((NavigationDrawerActivity) mActivity).setMarginVisibility(false);
            }

            @Override
            public void onLoadError(boolean arg0) {
                Log.i("APZ", "onLoadError " + arg0);
                ((NavigationDrawerActivity) mActivity).setMarginVisibility(false);
            }

            @Override
            public void onNotifySoundOn() {
                Log.i("APZ", "onNotifySoundOn");
                // onNotifySoundOn: indica che è stata completata l'erogazione di un'ADV con audio.
                // Nel caso di app con suono in background quest'ultimo può essere riattivato
            }

            @Override
            public void onNotifySoundOff() {
                Log.i("APZ", "onNotifySoundOff");
                // onNotifySoundOff: indica che è stato erogato un'ADV con audio. Nel caso di app
                // con suono in background quest'ultimo va disattivato.
            }

            @Override
            public void onNotifyBannerSize(BannerSizeProperties bannerSizeProperties) {
                Log.i("APZ", "onNotifyBannerSize");
                adViewToAdd.setLayoutParams(
                        new ViewGroup.LayoutParams(AdView.dpToPx(bannerSizeProperties.getWidth()),
                                AdView.dpToPx(bannerSizeProperties.getHeight())));
            }
        };
    }


    public void loadAdv(final Account account, final Me me, LinearLayout banner, View margin) {

        mLinearLayout = banner;

        // if (mAdViewToAdd != null) {
        // return;
        // }
        // if (mSubscriber != null) {
        // mSubscriber.unsubscribe();
        // }

        if (mLinearLayout.getChildCount() == 0
                && me.getAdv().getTiming().getMail().getShowtime() > 0) {
            addAdView(account);
        }

        // mSubscriber = Observable
        // .interval(me.getAdv().getTiming().getMail().getInterval(),
        // me.getAdv().getTiming().getMail().getShowtime(), TimeUnit.SECONDS)
        // .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Object>() {
        // @Override
        // public void onCompleted() {
        // Log.i("APZ", "onCompleted");
        // }
        //
        // @Override
        // public void onError(Throwable e) {
        // Log.i("APZ", "onError");
        // }
        //
        // @Override
        // public void onNext(Object o) {
        // Log.i("APZ", "onNext " + o);
        // addAdView(account);
        // }
        // });
    }

    // Da chiamare al CREATE ed al RESUME delle singole schermate che ospitano i banner!!!
    public static void onResume() {
        // final String mpoConfig = "sdk_android_mraid_testapp_adconfig";
        // final String mptConfig = "sdk_android";

        final String mpoConfig =
                K9.getAppContext().getResources().getString(R.string.dotandad_adv_mpo_smartphone);
        final String mptConfig = K9.getAppContext().getResources()
                .getString(R.string.dotandad_adv_mpt_smartphone_head);

        // Eventuali parametri esterni
        final Map<String, String> extParams = new HashMap<String, String>();

        final AdConfigListener adConfigListener = new AdConfigListener() {
            @Override
            public boolean onAdConfigLoaded(Map<String, AdSizeConfig> adsConfig) {
                // Config caricata posso preparare le griglie scorrendomi gli elementi si adsConfig
                Log.i("APZ", "onAdConfigLoaded");
                return true;
            }

            @Override
            public boolean onAdConfigError() {
                // Errore durante il load della config
                Log.i("APZ", "onAdConfigError");
                return false;
            }

        };
        DotAndMediaSDK.getInstance().setAdConfigListener(adConfigListener);
        DotAndMediaSDK.getInstance().loadAdSizeConfig(mpoConfig, mptConfig, extParams);
    }

}
