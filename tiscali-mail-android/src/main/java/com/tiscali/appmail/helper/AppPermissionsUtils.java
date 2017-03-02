package com.tiscali.appmail.helper;

/**
 * Created by Annalisa Sini on 01/03/2017.
 */

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Utility class to simplify the Marshmallow permissions implementation. You can use this for
 * checking permission, rationale and request the permission itself.
 *
 * Normal permissions grant result will be notified on the onRequestPermissionsResult() callback.
 */
public class AppPermissionsUtils {

    /** To be used whenever we need to check against a permission, before taking any action */
    public static boolean isPermissionGranted(Context context, String permission) {
        if (Build.VERSION.SDK_INT < 23) {
            // permissions are always granted
            return true;
        } else {
            int grant = ContextCompat.checkSelfPermission(context, permission);
            return (PackageManager.PERMISSION_GRANTED == grant);
        }
    }


    /** To be used in the onRequestPermissionsResult() callback */
    public static boolean isPermissionGranted(String permission, String[] permissions,
            int[] grantResults) {
        if (!isRequestCanceled(permissions, grantResults)) {
            // find the permission index
            int permissionIndex = Arrays.asList(permissions).indexOf(permission);
            if ((permissionIndex != -1)
                    && PackageManager.PERMISSION_GRANTED == grantResults[permissionIndex]) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRequestCanceled(String[] permissions, int[] grantResults) {
        return (permissions.length == 0) || (grantResults.length == 0);
    }


    /**
     * Gets whether you should show UI with rationale for requesting a permission. You should do
     * this only if you do not have the permission and the context in which the permission is
     * requested does not clearly communicate to the user what would be the benefit from granting
     * this permission.
     * <p>
     * For example, if you write a camera app, requesting the camera permission would be expected by
     * the user and no rationale for why it is requested is needed. If however, the app needs
     * location for tagging photos then a non-tech savvy user may wonder how location is related to
     * taking photos. In this case you may choose to show UI with rationale of requesting this
     * permission.
     * </p>
     *
     * @param permission A permission your app wants to request.
     * @return Whether you can show permission rationale UI. Always true for Api below 23
     */
    public static boolean shouldShowRequestPermissionRationale(Activity activity,
            String permission) {
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= 23) {
                return activity.shouldShowRequestPermissionRationale(permission);
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Use this on the onRequestPermissionsResult() callback when the permission was not granted to
     * check if the user denied flagging NEVER ASK AGAIN, you can open another dialog explaining
     * again the permission and directing to the app setting
     *
     * @param activity The context Activity
     * @param permission The required permission
     * @return Whether you should ask to grant permission showing the app settings page
     */
    public static boolean shouldAskToChangeSettings(Activity activity, String permission) {
        return !shouldShowRequestPermissionRationale(activity, permission);
    }

    /** Request a single permission from an Activity */
    public static void requestPermission(Activity activity, String permission, int requestCode) {
        requestPermissions(activity, new String[] {permission}, requestCode);
    }

    /** Request an array of permissions from an Activity */
    public static void requestPermissions(Activity activity, String[] permissions,
            int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    // /**
    // * Request a single permission from a Fragment. Can be used for special permissions
    // * (SYSTEM_ALERT_WINDOW, WRITE_SETTINGS)
    // */
    // public static void requestPermission(Fragment fragment, String permission, int requestCode) {
    // boolean handled = requestSpecialPermission(fragment, permission, requestCode);
    // if (!handled) {
    // requestPermissions(fragment, new String[] {permission}, requestCode);
    // }
    // }
    //
    // /**
    // * Request an array of permissions from a Fragment. Not suitable for special permissions
    // * (SYSTEM_ALERT_WINDOW, WRITE_SETTINGS)
    // */
    // public static void requestPermissions(Fragment fragment, String[] permissions,
    // int requestCode) {
    // // must call this to get notified
    // fragment.requestPermissions(permissions, requestCode);
    // }


    // private static boolean requestSpecialPermission(Fragment fragment, String permission,
    // int requestCode) {
    // if (Build.VERSION.SDK_INT >= 23) {
    // String action = null;
    // if (Manifest.permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
    // action = Settings.ACTION_MANAGE_OVERLAY_PERMISSION;
    // } else if (Manifest.permission.WRITE_SETTINGS.equals(permission)) {
    // action = Settings.ACTION_MANAGE_WRITE_SETTINGS;
    // }
    //
    // if (action != null) {
    // Intent intent = new Intent(action,
    // Uri.parse("package:" + Indoona.getContext().getPackageName()));
    // try {
    // fragment.startActivityForResult(intent, requestCode);
    // return true; // handled
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // return false; // to be handled by caller
    // }

}
