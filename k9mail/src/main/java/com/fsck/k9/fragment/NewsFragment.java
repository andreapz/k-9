package com.fsck.k9.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

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

import com.fsck.k9.R;


import java.util.HashMap;


/**
 * Created by thomascastangia on 02/01/17.
 */



public class NewsFragment extends Fragment {

    public static final String JAVASCRIPT_TISCALI_APP_GET_TITLE = "javascript:window.TiscaliApp.setTitle(tiscaliApp.getTitle)";
    private static final String ARG_HOME = "HOME";
    public static final String PLATFORM_ANDROID = "android";
    public static final String HEADER_X_TISCALI_APP = "X-Tiscali-App";
    public static final String JAVASCRIPT_TISCALI_APP_IS_SHAREABLE = "javascript:window.TiscaliApp.setShareable(tiscaliApp.isShareable)";
    public static final String JAVASCRIPT_TISCALI_APP_GET_ID_SECTION = "javascript:window.TiscaliApp.setIdSection(tiscaliApp.getIdSection)";
    public static final String JAVASCRIPT_TISCALI_APP_HAS_RESIZABLE_TEXT = "javascript:window.TiscaliApp.setResizableText(tiscaliApp.hasResizableText)";
    public static final String JAVASCRIPT_FIRST_PART_SET_PAGE_STATUS = "javascript:window.TiscaliApp.setResult(tiscaliApp.setPageStatus(";
    public static final String JAVASCRIPT_SECOND_PART_SET_PAGE_STATUS = "))";
    public static final String TISCALI_APP_GET_TITLE = "tiscaliApp.getTitle";
    public static final String TISCALI_APP = "TiscaliApp";
    private HashMap<String, String> mExtraHeaders;

    public WebView mWebView;
    public String home_url;
    private boolean mIsShareable = false;
    private NewsFragmentListener mFragmentListener;


    private String mIdSection;
    private boolean mIsResizable = false;
    private Menu mMenu;




    public  static NewsFragment newInstance(String home) {

        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOME, home);
        fragment.setArguments(args);
        return fragment;
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

        home_url = getArguments().getString(ARG_HOME);

        mFragmentListener = getFragmentListner();

        init();

        loadUrl(home_url);

        return v;
    }

    public void init() {
        mExtraHeaders = new HashMap<>();
        mExtraHeaders.put(HEADER_X_TISCALI_APP,PLATFORM_ANDROID);
        updateWebViewSettings();

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.d("TiscaliWebViewClient", "[URL]:" + url+" @"+this);

                if (mFragmentListener != null && !mFragmentListener.isDetailStatus()) {
                    mFragmentListener.detailPageLoad(url);
                    return true;
                }

                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(mFragmentListener != null) {
                    mFragmentListener.enableActionBarProgress(false);
                }
                view.loadUrl(JAVASCRIPT_TISCALI_APP_GET_TITLE);
                view.loadUrl(JAVASCRIPT_TISCALI_APP_IS_SHAREABLE);
                view.loadUrl(JAVASCRIPT_TISCALI_APP_GET_ID_SECTION);
                view.loadUrl(JAVASCRIPT_TISCALI_APP_HAS_RESIZABLE_TEXT);

                if(mFragmentListener.isHomePage()){
                    view.loadUrl(JAVASCRIPT_FIRST_PART_SET_PAGE_STATUS +mFragmentListener.getMeObject()+ JAVASCRIPT_SECOND_PART_SET_PAGE_STATUS);
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
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);

        //Todo check if version compliant
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
        if(mMenu == null){
//            getActivity().getMenuInflater().inflate(R.menu.news_menu_option, menu);
            mMenu = menu;
        }

        if (mIsShareable) {
            menu.findItem(R.id.menu_item_share).setVisible(true);
        }else{
            menu.findItem(R.id.menu_item_share).setVisible(false);
        }
        super.onCreateOptionsMenu(menu,menuInflater);
    }


    private void loadUrl(String url) {
        Log.d("TiscaliWebView","[URL]:"+url+" @"+this);

        mWebView.loadUrl(url, mExtraHeaders);
        mFragmentListener.enableActionBarProgress(true);
        mFragmentListener.setCurrentUrl(url);
    }

    private NewsFragmentListener getFragmentListner() {

        NewsFragmentListener listener = null;

        if(getActivity() instanceof NewsFragmentGetListener) {
            try {
                listener = ((NewsFragmentGetListener) getActivity()).getNewsFragmentListner();
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().getClass() +
                        " must implement NewsFragmentListener");
            }
        }

        return listener;
    }

    @SuppressLint("JavascriptInterface")
    public void updateUrl(String newUrl) {
        loadUrl(newUrl);
    }

    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    public void goBackOnHistory() {
        mWebView.goBack();
    }

    public void getTitle(){
        if(mWebView!= null){
            mWebView.loadUrl(JAVASCRIPT_TISCALI_APP_GET_TITLE);
        }

    }
    public void getSharable(){
        if(mWebView!= null){
            mWebView.loadUrl(JAVASCRIPT_TISCALI_APP_IS_SHAREABLE);
        }

    }
    public interface NewsFragmentListener {
        void enableActionBarProgress(boolean enable);
        void detailPageLoad(String url);
        boolean isDetailStatus();
        boolean isHomePage();
        String getMeObject();
        void setPageTitle(String title);
        void setActionBarToggle();
        void setCurrentUrl(String url);
        void setActionBarUp();
        void goBack();
    }

    public interface NewsFragmentGetListener {
        NewsFragmentListener getNewsFragmentListner();
    }

    class JsTiscaliAppObject {
        @JavascriptInterface
        public String toString() { return TISCALI_APP_GET_TITLE; }

        @JavascriptInterface
        public void setTitle(String value) {
            if (value!= null && value.length() > 0) {

                Log.d("TiscaliWebView","[TITLE]:"+value);
                if(mFragmentListener != null) {
                    mFragmentListener.setPageTitle(value);
                }
            }
        }
        @JavascriptInterface
        public void setShareable(boolean value) {
            Log.d("TiscaliWebView","[SHAREABLE]:"+value);
            mIsShareable = value;
            getActivity().invalidateOptionsMenu();
        }
        @JavascriptInterface
        public void setIdSection(String value) {
            Log.d("TiscaliWebView","[SECTION_ID]:"+value);
            mIdSection = value;
        }
        @JavascriptInterface
        public void setResizableText(boolean value) {
            Log.d("TiscaliWebView","[RESIZABLE]:"+value);
            mIsResizable = value;
        }
        @JavascriptInterface
        public void setResult(boolean value) {
            Log.d("TiscaliWebView","[SET]:"+value);

        }

    }
}