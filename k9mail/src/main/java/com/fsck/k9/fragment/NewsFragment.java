package com.fsck.k9.fragment;


import android.app.Fragment;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v=inflater.inflate(R.layout.news, container, false);
        mWebView = (WebView) v.findViewById(R.id.webview);

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        home_url = getArguments().getString(ARG_HOME);
        mWebView.loadUrl(home_url);
        mFragmentListener = getFragmentListner();
        mFragmentListener.enableActionBarProgress(true);

        // Force links and redirects to open in the WebView instead of in a browser

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){

                if(!mFragmentListener.isDetailStatus()){
                    mFragmentListener.detailPageLoad(url);
                    return true;
                }
                return false;
            }
            public void onPageFinished(WebView view, String url) {
                mFragmentListener.enableActionBarProgress(false);
            }
        });



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

    public void updateUrl(String newUrl) {

        mWebView.loadUrl(newUrl);
        mFragmentListener.enableActionBarProgress(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                if(!mFragmentListener.isDetailStatus()){
                    mFragmentListener.detailPageLoad(url);
                    return true;
                }
                return false;
            }
            public void onPageFinished(WebView view, String url) {
                mFragmentListener.enableActionBarProgress(false);
            }
        });
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