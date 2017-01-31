package com.fsck.k9.fragment;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import com.fsck.k9.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
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


public class NewsFragment extends Fragment {

    private static final String JAVASCRIPT_TISCALI_APP_GET_TITLE =
            "javascript:window.TiscaliApp.setTitle(tiscaliApp.getTitle)";
    private static final String ARG_HOME = "HOME";
    private static final String ARG_TYPE = "TYPE";
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
    private HashMap<String, String> mExtraHeaders;

    public WebView mWebView;
    private String mUrl;
    private boolean mIsShareable = false;
    private NewsFragmentListener mFragmentListener;


    private String mIdSection;
    private boolean mIsResizable = false;
    private Menu mMenu;
    private MediaPresenter.Type mType;

    public static NewsFragment newInstance(String home, MediaPresenter.Type type) {

        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOME, home);
        args.putString(ARG_TYPE, type.name());
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
        mWebView = (WebView) v.findViewById(R.id.webview);
        if (savedInstanceState == null) {
            mType = getType(getArguments().getString(ARG_TYPE));

        } else {
            mType = getType(savedInstanceState.getString(ARG_TYPE));
        }
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_URL, mUrl);
        outState.putString(ARG_TYPE, mType.name());
        mWebView.saveState(outState);
    }

    private void updateWebViewSettings() {
        // Enable Javascript
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

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

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("Reload TiscaliWebView", "[URL]:" + mUrl + " @" + this);
                    if (mUrl != null) {
                        loadUrl(mUrl);
                    }
                }
            }, mFragmentListener.getRefreshTimeout());
        }
    }

    private NewsFragmentListener getFragmentListner() {

        NewsFragmentListener listener = null;

        if (getActivity() instanceof NewsFragmentGetListener) {
            try {
                listener = ((NewsFragmentGetListener) getActivity()).getNewsFragmentListner(mType);
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

    public void refreshUrl() {
        if (mWebView != null) {
            loadUrl(mUrl);
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

    public interface NewsFragmentListener {
        void enableActionBarProgress(boolean enable);

        void detailPageLoad(String url);

        boolean isDetailStatus();

        boolean isHomePage();

        String getMeJSON();

        int getRefreshTimeout();

        void setPageTitle(String title);

        void setFavoriteSection(String sectionId, boolean value);

        void setActionBarToggle();

        void setCurrentUrl(String url);

        void setActionBarUp();

        void goBack();
    }

    public interface NewsFragmentGetListener {

        NewsFragmentListener getNewsFragmentListner(MediaPresenter.Type type);
    }

    class JsTiscaliAppObject {
        @JavascriptInterface
        public String toString() {
            return TISCALI_APP_GET_TITLE;
        }

        @JavascriptInterface
        public void setTitle(String value) {
            if (value != null && value.length() > 0) {

                Log.d("TiscaliWebView", "[TITLE]:" + value);
                if (mFragmentListener != null) {
                    mFragmentListener.setPageTitle(value);
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
            if (mFragmentListener.isHomePage()) {
                Uri uri = Uri.parse(url);
                String lastSegment = uri.getLastPathSegment();
                if (lastSegment.compareTo(TISCALI_APP_FAVE_SEGMENT) == 0) {
                    String sessionId = uri.getQueryParameter(TISCALI_APP_FAVE_SECTIONID_PARAMS);
                    String value = uri.getQueryParameter(TISCALI_APP_FAVE_FAV_PARAMS);
                    mFragmentListener.setFavoriteSection(sessionId, Boolean.parseBoolean(value));
                    return true;
                }
            }


            if (mFragmentListener != null && !mFragmentListener.isDetailStatus()) {
                mFragmentListener.detailPageLoad(url);
                return true;
            }

            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (mFragmentListener != null) {
                mFragmentListener.enableActionBarProgress(false);
            }
            view.loadUrl(JAVASCRIPT_TISCALI_APP_GET_TITLE);
            view.loadUrl(JAVASCRIPT_TISCALI_APP_IS_SHAREABLE);
            view.loadUrl(JAVASCRIPT_TISCALI_APP_GET_ID_SECTION);
            view.loadUrl(JAVASCRIPT_TISCALI_APP_HAS_RESIZABLE_TEXT);

            if (mFragmentListener.isHomePage()) {
                NewsFragmentListener listener = getFragmentListner();
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
