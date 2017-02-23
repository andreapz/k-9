package com.tiscali.appmail.fragment;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Timer;

import com.tiscali.appmail.R;
import com.tiscali.appmail.activity.BrowserActivity;
import com.tiscali.appmail.helper.NetworkHelper;
import com.tiscali.appmail.view.ObservableWebView;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by thomascastangia on 02/01/17.
 */


public class MediaFragment extends Fragment {

    private static final String JAVASCRIPT_TISCALI_APP_GET_TITLE =
            "javascript:window.TiscaliApp.setTitle(tiscaliApp.getTitle)";
    private static final String ARG_HOME = "HOME";
    private static final String ARG_TYPE = "TYPE";
    private static final String ARG_BOTTOM_BAR = "ARG_BOTTOM_BAR";
    public static final String PLATFORM_ANDROID = "android";
    public static final String HEADER_X_TISCALI_APP = "X-Tiscali-App";
    public static final String CURRENT_URL = "CURRENT_URL";


    private static final String JAVASCRIPT_PREFIX = "javascript:";
    private static final String JAVASCRIPT_TISCALI_APP_PREFIX = "window.TiscaliApp";

    private static final String JAVASCRIPT_TISCALI_APP_IS_SHAREABLE = JAVASCRIPT_PREFIX
            + JAVASCRIPT_TISCALI_APP_PREFIX + ".setShareable(tiscaliApp.isShareable)";
    private static final String JAVASCRIPT_TISCALI_APP_GET_ID_SECTION = JAVASCRIPT_PREFIX
            + JAVASCRIPT_TISCALI_APP_PREFIX + ".setIdSection(tiscaliApp.getIdSection)";
    private static final String JAVASCRIPT_TISCALI_APP_HAS_RESIZABLE_TEXT = JAVASCRIPT_PREFIX
            + JAVASCRIPT_TISCALI_APP_PREFIX + ".setResizableText(tiscaliApp.hasResizableText)";
    private static final String JAVASCRIPT_TISCALI_APP_SET_PAGE_STATUS = JAVASCRIPT_PREFIX
            + JAVASCRIPT_TISCALI_APP_PREFIX + ".setResult(tiscaliApp.setPageStatus(\"%D\"))";

    private static final String TISCALI_APP_GET_TITLE = "tiscaliApp.getTitle";
    private static final String TISCALI_APP = "TiscaliApp";

    private static final String TISCALI_APP_FAVE_SEGMENT = "hookTiscaliApp";
    private static final String TISCALI_APP_FAVE_SECTIONID_PARAMS = "section_id";
    private static final String TISCALI_APP_FAVE_FAV_PARAMS = "fav";
    private static final String TISCALI_APP_FAVE_FAVE_ACTION = "section_fave";

    private static final String URL_ADV_MARGIN_100 =
            "javascript:(function(){$(\"#adv-footer-container\").css(\"margin-bottom\",\"100px\");})";
    private static final String URL_ADV_MARGIN_0 =
            "javascript:(function(){$(\"#adv-footer-container\").css(\"margin-bottom\",\"0px\");})";

    private HashMap<String, String> mExtraHeaders;

    public ObservableWebView mWebView;
    private String mUrl;
    private boolean mIsShareable = false;
    private MediaFragmentListener mFragmentListener;


    private String mIdSection;
    private boolean mIsResizable = false;

    private Menu mMenu;
    private MediaPresenter.Type mType;
    private Timer mTimerRefresh;
    private Handler mHandler;
    private boolean mIsBarVisible;


    public static MediaFragment newInstance(String home, MediaPresenter.Type type,
            boolean isBarVisible) {

        MediaFragment fragment = new MediaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOME, home);
        args.putString(ARG_TYPE, type.name());
        args.putBoolean(ARG_BOTTOM_BAR, isBarVisible);
        fragment.setArguments(args);
        return fragment;
    }

    public void setType(MediaPresenter.Type type) {
        this.mType = type;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This fragments adds options to the action bar
        setHasOptionsMenu(true);
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.news, container, false);
        mWebView = (ObservableWebView) v.findViewById(R.id.webview);
        mHandler = new Handler();
        // if (savedInstanceState == null) {
        mType = getType(getArguments().getString(ARG_TYPE));
        mIsBarVisible = getArguments().getBoolean(ARG_BOTTOM_BAR);
        // } else {
        // mType = getType(savedInstanceState.getString(ARG_TYPE));
        // mIsBarVisible = savedInstanceState.getBoolean(ARG_BOTTOM_BAR);
        // }
        mFragmentListener = getFragmentListner();

        init();

        if (savedInstanceState == null) {
            mUrl = getArguments().getString(ARG_HOME);
            loadUrl(mUrl);
        } else {
            mUrl = savedInstanceState.getString(CURRENT_URL);
            mWebView.restoreState(savedInstanceState);
        }

        return v;
    }

    private MediaPresenter.Type getType(String type) {
        if (MediaPresenter.Type.VIDEO.name().equals(type)) {
            return MediaPresenter.Type.VIDEO;
        }
        if (MediaPresenter.Type.OFFERS.name().equals(type)) {
            return MediaPresenter.Type.OFFERS;
        }
        return MediaPresenter.Type.NEWS;
    }

    public void init() {
        mExtraHeaders = new HashMap<>();
        mExtraHeaders.put(HEADER_X_TISCALI_APP, PLATFORM_ANDROID);
        updateWebViewSettings();

        mWebView.setWebChromeClient(new WebChromeClient());


        mWebView.setWebViewClient(new TiscaliWebClient());

        if (mIsBarVisible) {
            mWebView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
                public boolean mIsBottomNavVisible = false;
                public int mLastFirstVisibleItem = 1000;

                @Override
                public void onScroll(int l, int t) {
                    if (t > mLastFirstVisibleItem) {
                        if (mIsBottomNavVisible) {
                            mIsBottomNavVisible = false;
                            mFragmentListener.hideBottomNav();
                            mWebView.loadUrl(URL_ADV_MARGIN_0);
                        }
                    } else if (t < mLastFirstVisibleItem) {
                        if (!mIsBottomNavVisible) {
                            mIsBottomNavVisible = true;
                            mFragmentListener.showBottomNav();
                            mWebView.loadUrl(URL_ADV_MARGIN_100);
                        }
                    }

                    mLastFirstVisibleItem = t;
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // super.onSaveInstanceState(outState);
        // outState.putString(CURRENT_URL, mUrl);
        // outState.putString(ARG_TYPE, mType.name());
        // outState.putBoolean(ARG_BOTTOM_BAR, mIsBarVisible);
        // mWebView.saveState(outState);
    }

    private void updateWebViewSettings() {
        // Enable Javascript
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        // settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setSupportZoom(false);

        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // Todo check if version compliant
        mWebView.addJavascriptInterface(new JsTiscaliAppObject(), TISCALI_APP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                mFragmentListener.goBack();
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        if (mMenu == null) {
            mMenu = menu;
        }

        if (mIsShareable) {
            menu.findItem(R.id.menu_item_share).setVisible(true);
        } else {
            menu.findItem(R.id.menu_item_share).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, menuInflater);
    }


    private void loadUrl(String url) {
        Log.d("TiscaliWebView", "[URL]:" + url + " @" + this);
        if (mWebView != null && mFragmentListener != null) {
            mWebView.loadUrl(url, mExtraHeaders);
            mFragmentListener.enableActionBarProgress(true);
            mFragmentListener.setCurrentUrl(url);
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Reload TiscaliWebView", "[URL]:" + mUrl + " @" + this);
                        if (mUrl != null) {
                            if (getActivity() != null
                                    && NetworkHelper.getInstance(getActivity()).isConnected()) {
                                loadUrl(mUrl);
                            }
                        }
                    }
                }, mFragmentListener.getRefreshTimeout());
            }
        }
    }

    private MediaFragmentListener getFragmentListner() {

        MediaFragmentListener listener = null;

        if (getActivity() instanceof MediaFragmentGetListener) {
            try {
                listener =
                        ((MediaFragmentGetListener) getActivity()).getMediaFragmentListener(mType);
            } catch (ClassCastException e) {
                throw new ClassCastException(
                        getActivity().getClass() + " must implement NewsFragmentListener");
            }
        }

        return listener;
    }

    @SuppressLint("JavascriptInterface")
    public void updateUrl(String newUrl) {
        mUrl = newUrl;
        loadUrl(newUrl);
    }

    public void setPageStatus() {
        if (mWebView != null && mFragmentListener != null) {
            String value = mFragmentListener.getMeJSON();
            try {
                String valueEncoded = URLEncoder.encode(value, "UTF-8").replace("%", "\\x");
                mWebView.loadUrl(
                        JAVASCRIPT_TISCALI_APP_SET_PAGE_STATUS.replace("%D", valueEncoded));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    public void goBackOnHistory() {
        mWebView.goBack();
    }

    public void getTitle() {
        if (mWebView != null) {
            mWebView.loadUrl(JAVASCRIPT_TISCALI_APP_GET_TITLE);
        }

    }

    public void getSharable() {
        if (mWebView != null) {
            mWebView.loadUrl(JAVASCRIPT_TISCALI_APP_IS_SHAREABLE);
        }

    }

    public interface MediaFragmentListener {
        void enableActionBarProgress(boolean enable);

        void detailPageLoad(String url);

        boolean isDetailStatus();

        boolean isHomePage();

        boolean isExternalBrowsing();

        void setExternalBrowsing(boolean value);

        boolean isWalledGarden(String domain);

        String getMeJSON();

        int getRefreshTimeout();

        void setPageTitle(String title);

        void setFavoriteSection(String sectionId, boolean value);

        void setActionBarToggle();

        void setCurrentUrl(String url);

        void setActionBarUp();

        void goBack();

        void showBottomNav();

        void hideBottomNav();
    }

    public interface MediaFragmentGetListener {

        MediaFragmentListener getMediaFragmentListener(MediaPresenter.Type mType);
    }

    class JsTiscaliAppObject {
        @JavascriptInterface
        public String toString() {
            return TISCALI_APP_GET_TITLE;
        }

        @JavascriptInterface
        public void setTitle(String value) {
            if (value != null && value.length() > 0) {

                String title = null;
                try {
                    title = URLDecoder.decode(value, "UTF8");
                    Log.d("TiscaliWebView", "[TITLE]:" + title);
                    if (mFragmentListener != null) {
                        mFragmentListener.setPageTitle(title);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        @JavascriptInterface
        public void setShareable(boolean value) {
            Log.d("TiscaliWebView", "[SHAREABLE]:" + value);
            mIsShareable = value;
            if (getActivity() != null) {
                getActivity().invalidateOptionsMenu();
            }
        }

        @JavascriptInterface
        public void setIdSection(String value) {
            Log.d("TiscaliWebView", "[SECTION_ID]:" + value);
            mIdSection = value;
        }

        @JavascriptInterface
        public void setResizableText(boolean value) {
            Log.d("TiscaliWebView", "[RESIZABLE]:" + value);
            mIsResizable = value;
        }

        @JavascriptInterface
        public void setResult(boolean value) {
            Log.d("TiscaliWebView", "[SET]:" + value);
        }

    }

    private class TiscaliWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Log.d("TiscaliWebViewClient", "[URL]:" + url + " @" + this);
            Uri uri = Uri.parse(url);
            if (mFragmentListener != null && mFragmentListener.isHomePage()) {

                String lastSegment = uri.getLastPathSegment();
                if (lastSegment != null && lastSegment.compareTo(TISCALI_APP_FAVE_SEGMENT) == 0) {
                    String sessionId = uri.getQueryParameter(TISCALI_APP_FAVE_SECTIONID_PARAMS);
                    String value = uri.getQueryParameter(TISCALI_APP_FAVE_FAV_PARAMS);
                    mFragmentListener.setFavoriteSection(sessionId, Boolean.parseBoolean(value));
                    return true;
                }
            }

            if (uri.getHost() != null && mFragmentListener.isWalledGarden(uri.getHost())) {
                if (mFragmentListener != null && !mFragmentListener.isDetailStatus()) {
                    mFragmentListener.detailPageLoad(url);
                    return true;
                }
            } else if (!mFragmentListener.isExternalBrowsing()) {
                Intent myIntent = new Intent(getActivity(), BrowserActivity.class);
                myIntent.putExtra(BrowserActivity.EXTRA_URL, url); // Optional parameters
                getActivity().startActivityForResult(myIntent,
                        MediaPresenter.MEDIA_PRESENTER_BROWSING);
                mFragmentListener.setExternalBrowsing(true);
                return true;

            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (mFragmentListener != null) {
                mFragmentListener.enableActionBarProgress(false);
            }
            if (mType.equals(MediaPresenter.Type.OFFERS)) {
                String title = view.getTitle();
                if (title.contains(" ")) {
                    title = title.substring(0, title.indexOf(" "));
                }
                String titleEncode = title.replaceAll("%20", " ");
                if (mFragmentListener != null) {
                    mFragmentListener.setPageTitle(titleEncode);
                }
            } else {
                view.loadUrl(JAVASCRIPT_TISCALI_APP_GET_TITLE);
            }

            view.loadUrl(JAVASCRIPT_TISCALI_APP_IS_SHAREABLE);
            view.loadUrl(JAVASCRIPT_TISCALI_APP_GET_ID_SECTION);
            view.loadUrl(JAVASCRIPT_TISCALI_APP_HAS_RESIZABLE_TEXT);

            if (mFragmentListener.isHomePage()) {
                MediaFragmentListener listener = getFragmentListner();
                if (listener != null) {
                    String value = listener.getMeJSON();
                    try {
                        String valueEncoded = URLEncoder.encode(value, "UTF-8").replace("%", "\\x");
                        view.loadUrl(
                                JAVASCRIPT_TISCALI_APP_SET_PAGE_STATUS.replace("%D", valueEncoded));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
