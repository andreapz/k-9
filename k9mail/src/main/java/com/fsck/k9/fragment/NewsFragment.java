package com.fsck.k9.fragment;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fsck.k9.R;

import java.util.HashMap;


/**
 * Created by thomascastangia on 02/01/17.
 */



public class NewsFragment extends Fragment {

    private static final String ARG_HOME = "HOME";
    public static final String PLATFORM_ANDROID = "android";
    public static final String HEADER_X_TISCALI_APP = "X-Tiscali-App";
    private HashMap<String, String> mExtraHeaders;

    public WebView mWebView;
    public String home_url;
    private NewsFragmentListener mFragmentListener;


    public  static NewsFragment newInstance(String home) {

        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOME, home);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.news, container, false);
        mWebView = (WebView) v.findViewById(R.id.webview);

        home_url = getArguments().getString(ARG_HOME);

        mFragmentListener = getFragmentListner();
        loadUrl(home_url);
        init();



        return v;
    }

    public void init() {
        mExtraHeaders = new HashMap<String, String>();
        mExtraHeaders.put(HEADER_X_TISCALI_APP,PLATFORM_ANDROID);
        updateWebViewSettings();
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.d("TiscaliWebViewClient", "[URL]:" + url+" @"+this);

                if (mFragmentListener != null && !mFragmentListener.isDetailStatus()) {
                    mFragmentListener.detailPageLoad(url);
                    return false;
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.TiscaliApp.setTitle(tiscaliApp.getTitle)");
                if(mFragmentListener != null) {
                    mFragmentListener.enableActionBarProgress(false);
                }
            }
        });
    }

    private void updateWebViewSettings() {
        // Enable Javascript
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);

        //Todo check if version compliant
        mWebView.addJavascriptInterface(new JsTiscaliAppObject(), "TiscaliApp");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
    }

    private void loadUrl(String url) {
        Log.d("TiscaliWebView","[URL]:"+url+" @"+this);
        mWebView.loadUrl(url, mExtraHeaders);
        mFragmentListener.enableActionBarProgress(true);
    }

    private NewsFragmentListener getFragmentListner() {

        NewsFragmentListener listener = null;

        if(getActivity() instanceof NewsFragmentGetListener) {
            try {
                listener = ((NewsFragmentGetListener) getActivity()).getNewsFragmentListner();
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().getClass() +
                        " must implement MessageListFragmentListener");
            }
        }

        return listener;
    }

    @SuppressLint("JavascriptInterface")
    public void updateUrl(String newUrl) {
        loadUrl(newUrl);
    }


    public interface NewsFragmentListener {
        void enableActionBarProgress(boolean enable);
        void detailPageLoad(String url);
        boolean isDetailStatus();
        void setActionBarToggle();
        void setActionBarTitle(String title);
        void setActionBarUp();
        void goBack();
     }
    public interface NewsFragmentGetListener {
        NewsFragmentListener getNewsFragmentListner();
    }

    class JsTiscaliAppObject {
        @JavascriptInterface
        public String toString() { return "tiscaliApp.getTitle"; }

        @JavascriptInterface
        public void setTitle(String value) {
            if (value.length() > 0) {

                if(mFragmentListener != null && !TextUtils.isEmpty(value)) {
                    Log.d("TiscaliWebView","[TITLE]:"+value);
                    mFragmentListener.setActionBarTitle(value);
                }
            }
        }
    }
}