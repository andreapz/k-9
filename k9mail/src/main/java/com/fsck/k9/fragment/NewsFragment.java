package com.fsck.k9.fragment;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fsck.k9.R;


/**
 * Created by thomascastangia on 02/01/17.
 */



public class NewsFragment extends Fragment {

    public WebView mWebView;
    public String home_url;
    public  static NewsFragment newInstance(String home) {

        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString("home", home);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v=inflater.inflate(R.layout.fragment_news, container, false);
        mWebView = (WebView) v.findViewById(R.id.webview);

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        home_url = getArguments().getString("home");
        mWebView.loadUrl(home_url);
        // Force links and redirects to open in the WebView instead of in a browser

        mWebView.setWebViewClient(new WebViewClient(){
            public void onProgressChanged(WebView view, int progress) {
                //nop
            }
        });



        return v;
    }

    public void updateUrl(String newUrl) {

        mWebView.loadUrl(newUrl);
        mWebView.setWebViewClient(new WebViewClient());
    }


}