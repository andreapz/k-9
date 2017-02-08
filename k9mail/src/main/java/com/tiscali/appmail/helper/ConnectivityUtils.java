package com.tiscali.appmail.helper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by thomascastangia on 02/02/17.
 */

public class ConnectivityUtils {

    private static final String CAPTIVE_PORTAL_GOOGLE_GENERATE_204_URL =
            "http://clients3.google.com/generate_204";
    private static final int CAPTIVE_PORTAL_SOCKET_TIMEOUT_MS = 10000;

    /**
     * Get the network info
     * 
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     * 
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = ConnectivityUtils.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     * 
     * @param context
     * @return
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = ConnectivityUtils.getNetworkInfo(context);
        return (info != null && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     * 
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = ConnectivityUtils.getNetworkInfo(context);
        return (info != null && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity
     * 
     * @param context
     * @return
     */
    public static boolean isConnectedFast(Context context) {
        NetworkInfo info = ConnectivityUtils.getNetworkInfo(context);
        return (info != null && info.isConnected()
                && ConnectivityUtils.isConnectionFast(info.getType(), info.getSubtype()));
    }

    /**
     * Check if the connection is fast
     * 
     * @param type
     * @param subType
     * @return
     */
    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion to appropriate level
                 * to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }


    /**
     * Opens and close a socket to the specified host:port, returns the time required to open
     * 
     * @param host
     * @param port
     * @return the required time to make the socket connection in mills. A negative value indicates
     *         an error condition.
     */
    public static long testConnetctionTime(String host, int port, int timeout) {
        try {
            long start = System.nanoTime();

            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress, timeout);

            long time = (System.nanoTime() - start) / 1000000; // milliseconds
            Log.v("test socket", "Connection made in " + time + " ms");

            socket.close();

            return time;

        } catch (IOException e) {
            Log.v("test socket", "Something goes wrong, exception: " + e);
            return -1;
        } catch (IllegalStateException e) {
            Log.v("test socket", "Something goes wrong, exception: " + e);
            return -1;
        }
    }


    /**
     * Check if we are behind a captive portal (simple check expecting an Http 204 response)
     * 
     * @return true if found
     */
    public static boolean isCaptivePortalConnection() {

        return isCaptivePortalConnection(CAPTIVE_PORTAL_GOOGLE_GENERATE_204_URL);
    }

    /**
     * Check if we are behind a captive portal (simple check expecting an Http 204 response)
     * 
     * @return true if found
     */
    // Based on http://stackoverflow.com/a/14030276/3214434
    public static boolean isCaptivePortalConnection(String http204NoContentUrl) {
        if (TextUtils.isEmpty(http204NoContentUrl)) {
            http204NoContentUrl = CAPTIVE_PORTAL_GOOGLE_GENERATE_204_URL;
        }
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(CAPTIVE_PORTAL_GOOGLE_GENERATE_204_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(CAPTIVE_PORTAL_SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(CAPTIVE_PORTAL_SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            // We got a valid response, but not from the real google
            return urlConnection.getResponseCode() != 204;
        } catch (IOException e) {
            Log.d("Captive portal check", "probably not a portal: exception " + e);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }


    }



}

