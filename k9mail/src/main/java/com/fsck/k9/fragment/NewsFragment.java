package com.fsck.k9.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mWebView = (WebView) mWebView.findViewById(R.id.webview);


        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Force links and redirects to open in the WebView instead of in a browser
        updateUrl("google.com");

        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    public void updateUrl(String newUrl) {

        mWebView.loadUrl(newUrl);
        mWebView.setWebViewClient(new WebViewClient());
    }


}