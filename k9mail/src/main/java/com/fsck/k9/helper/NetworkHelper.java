package com.fsck.k9.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by thomascastangia on 02/02/17.
 */

public class NetworkHelper {

    private static final String TAG = NetworkHelper.class.getCanonicalName();

    public static final String NETWORK_NAME_MOBILE = "generic.mobile";

    public static final String ACTION_NETWORK_CONNECTIVITY_CHANGE =
            TAG + ".ACTION_NETWORK_CONNECTIVITY";

    private static NetworkHelper sInstance;
    private final Context mContext;

    private WifiManager mWifiManager;
    private WifiManager.WifiLock mWifiLock;
    private boolean mAcquired;



    public static NetworkHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NetworkHelper(context);
        }
        return sInstance;
    }

    public static void resetInstance(Context context) {
        if (sInstance != null) {
            context.unregisterReceiver(sInstance.mConnectivityBroadcastReceiver);
            sInstance = null;
        }
    }

    private BroadcastReceiver mConnectivityBroadcastReceiver;


    /**
     * the last connection state broadcasted to the world
     */
    private boolean mConnected;

    public boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        if (network != null) {
            return network.isConnected();
        }
        return false;
    }


    private NetworkHelper(Context context) {

        // set the initial state
        mContext = context;
        mConnected = isConnected();


        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        mConnectivityBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    // debug only
                    boolean noConnectivity = intent
                            .getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                    boolean isFailover =
                            intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
                    String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);

                    Log.v(TAG, "Connectivity action:" + " no_connectivity=" + noConnectivity
                            + " is_failover=" + isFailover + " reason=" + reason);

                    if (mConnected != isConnected()) {
                        // connection state changed
                        mConnected = !mConnected;
                        LocalBroadcastManager.getInstance(mContext)
                                .sendBroadcast(new Intent(ACTION_NETWORK_CONNECTIVITY_CHANGE));
                    }
                }
            }
        };

        IntentFilter connectivityIntentFilter = new IntentFilter();
        connectivityIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mConnectivityBroadcastReceiver, connectivityIntentFilter);


    }


    /**
     * @return For TYPE_WIFI return the SSID of the current enabled wifi network. For TYPE_MOBILE or
     *         TYPE_WIMAX return a dummy name.
     */
    public String getCurrentNetworkName() {

        ConnectivityManager connectivityManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            Log.e(TAG, "Failed to get Network information");
            return null;
        }

        int netType = networkInfo.getType();
        if (netType == ConnectivityManager.TYPE_WIFI) {
            if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
                final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    // the ssid as network name
                    return wifiInfo.getSSID();
                }
            } else {
                Log.d(TAG, "WiFi not enabled");
            }
        } else if (netType == ConnectivityManager.TYPE_MOBILE
                || netType == ConnectivityManager.TYPE_WIMAX) {
            // a custom name for the mobile network
            return NETWORK_NAME_MOBILE;
        }
        return null;
    }



    public boolean release() {
        if (mWifiLock != null) {
            if (mWifiLock.isHeld()) {
                Log.d(TAG, "releaseNetworkLock()");
                mWifiLock.release();
            }
            mWifiLock = null;
        }

        mAcquired = false;
        return true;
    }


}

