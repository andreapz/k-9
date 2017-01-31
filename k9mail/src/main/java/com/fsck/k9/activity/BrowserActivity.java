package com.fsck.k9.activity;

import java.util.HashMap;

import com.fsck.k9.R;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by thomascastangia on 30/01/17.
 */

public class BrowserActivity extends K9Activity {

    public static final String EXTRA_URL = "BrowserActivity_URL";
    public static final String PLATFORM_ANDROID = "android";
    public static final String HEADER_X_TISCALI_APP = "X-Tiscali-App";
    private WebView mWebView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar mActionBar;
    private TextView mActionBarTitle;
    private ProgressBar mActionBarProgress;
    private HashMap<String, String> mExtraHeaders;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Bundle extras = getIntent().getExtras();
        String url = null;

        if (extras != null) {
            url = extras.getString(EXTRA_URL);
        }
        initializeActionBar();
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                mActionBarTitle.setText(view.getTitle());
                mActionBarProgress.setVisibility(View.INVISIBLE);

            }
        });

        updateWebViewSettings();
        if (url != null) {
            mActionBarProgress.setVisibility(View.VISIBLE);
            mWebView.loadUrl(url, mExtraHeaders);
        }


    }

    private void initializeActionBar() {
        mActionBar = getSupportActionBar();

        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.actioncustombar_news);

        View customView = mActionBar.getCustomView();
        mActionBarTitle = (TextView) customView.findViewById(R.id.actionbar_title_first);
        mActionBarProgress = (ProgressBar) customView.findViewById(R.id.actionbar_progress);

        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void updateWebViewSettings() {
        mExtraHeaders = new HashMap<>();
        mExtraHeaders.put(HEADER_X_TISCALI_APP, PLATFORM_ANDROID);
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
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // // Todo check if version compliant
        // mWebView.addJavascriptInterface(new MediaFragment.JsTiscaliAppObject(), TISCALI_APP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent returnIntent = getIntent();
        setResult(RESULT_OK, returnIntent);
        finish();
        return true;

    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            Intent returnIntent = getIntent();
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }
}
