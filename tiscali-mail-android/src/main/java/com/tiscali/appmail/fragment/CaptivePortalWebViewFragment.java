package com.tiscali.appmail.fragment;

import com.tiscali.appmail.R;
import com.tiscali.appmail.helper.CaptivePortalHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by thomascastangia on 02/02/17.
 */

public class CaptivePortalWebViewFragment extends Fragment {

    private static final String TAG = CaptivePortalWebViewFragment.class.getCanonicalName();
    public static final String CAPTIVE_PORTAL_URL =
            " https://tiscaliapp-api.tiscali.it/1/probe/welcome";
    public static final String EXTRA_URL = "EXTRA_URL";

    protected String mUrl = null;
    protected WebView webView;
    protected ProgressBar progressBar;
    protected ActionBar actionBar;

    private String mUrlDone = null;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);



        mUrl = CAPTIVE_PORTAL_URL;
        mUrlDone = CAPTIVE_PORTAL_URL;

        if ((mUrl == null) || (mUrlDone == null)) {
            // something goes wrong
            getActivity().finish();
            Log.w(TAG, "Finishing due to error. mUrl=" + mUrl + ", mUrlDone=" + mUrlDone);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // still behind captive portal?
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (!CaptivePortalHelper.getInstance(getActivity()).isCaptivePortalConnection()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            // close the activity
                            if (getActivity() != null) {
                                getActivity().finish();
                                Log.d(TAG,
                                        "Finishing due to missing condition. No more behind a captive portal.");
                            }
                        }
                    });
                }
            }
        }).start();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_web_view, container, false);



        webView = (WebView) view.findViewById(R.id.web_view);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        webView.loadUrl(mUrl);
        webView.getSettings().setJavaScriptEnabled(true);


        webView.setWebViewClient(getWebViewClient());

        // BugFix: http://code.google.com/p/android/issues/detail?id=7189
        webView.requestFocus(View.FOCUS_DOWN);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });


        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(TAG,
                        cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
                return true;
            }
        });


        return view;
    }

    protected WebViewClient getWebViewClient() {

        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                    String failingUrl) {

                if (getActivity() != null) {
                    view.loadUrl("about:blank");
                    new AlertDialog.Builder(getActivity()).setTitle("Error")
                            .setMessage("Unable to load content")
                            .setPositiveButton(android.R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Activity activity = getActivity(); // current activity
                                                                               // hosting
                                            // the fragment

                                            activity.finish();
                                        }
                                    })

                            .setIcon(android.R.drawable.ic_dialog_alert).show();

                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.v(TAG, "WebView page started, url: " + url);
                super.onPageStarted(view, url, favicon);

                // close when done
                if ((url != null) && url.startsWith(mUrlDone)) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                    return;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.v(TAG, "WebView page finished, url: " + url);
                super.onPageFinished(view, url);
            }

        };

        return webViewClient;
    }

}
