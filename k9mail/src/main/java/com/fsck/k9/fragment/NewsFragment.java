package com.fsck.k9.fragment;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fsck.k9.R;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by thomascastangia on 02/01/17.
 */



public class NewsFragment extends Fragment {

    private static final String ARG_HOME = "HOME";
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

        View v=inflater.inflate(R.layout.news, container, false);
        mWebView = (WebView) v.findViewById(R.id.webview);

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        home_url = getArguments().getString(ARG_HOME);
        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("X-Tiscali-App","iPhone");
        mWebView.loadUrl(home_url,extraHeaders);
        mFragmentListener = getFragmentListner();
        mFragmentListener.enableActionBarProgress(true);


        // register class containing methods to be exposed to JavaScript

//        mWebView.addJavascriptInterface(new Object(){
//            @JavascriptInterface
//            public void ongetTitle(){
//                Log.d("JS", "test");
//            }
//        },"Android");
        mWebView.setWebViewClient(new WebViewClient(){
            //            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){

                if(!mFragmentListener.isDetailStatus()){
                    mFragmentListener.detailPageLoad(url);
                    return true;
                }
                return false;
            }
            public void onPageFinished(WebView view, String url) {
//                view.loadUrl("javascript:tiscaliApp.getTitle");
                mFragmentListener.enableActionBarProgress(false);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient());
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            // In KitKat+ you should use the evaluateJavascript method
//            mWebView.evaluateJavascript("(tiscaliApp.getTitle() { return \"this\"; })();", new ValueCallback<String>() {
//                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//                @Override
//                public void onReceiveValue(String s) {
//                    Log.d("JS", s);
//                }
//            });
//        }



        return v;
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
        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("X-Tiscali-App","iPhone");
        mWebView.loadUrl(newUrl,extraHeaders);
        mFragmentListener.enableActionBarProgress(true);

//        mWebView.addJavascriptInterface(new Object(){
//            @JavascriptInterface
//            public void ongetTitle(){
//                Log.d("JS", "test");
//            }
//        },"Android");
        mWebView.setWebViewClient(new WebViewClient(){
            //            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){

                if(!mFragmentListener.isDetailStatus()){
                    mFragmentListener.detailPageLoad(url);
                    return true;
                }
                return false;
            }
            public void onPageFinished(WebView view, String url) {
//                view.loadUrl("javascript:tiscaliApp.getTitle");
                mFragmentListener.enableActionBarProgress(false);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient());
    }


    public interface NewsFragmentListener {
        void enableActionBarProgress(boolean enable);
        void detailPageLoad(String url);
        boolean isDetailStatus();
        void setActionBarToggle();
        void setActionBarUp();
        void goBack();
     }
    public interface NewsFragmentGetListener {
        NewsFragmentListener getNewsFragmentListner();
    }
}