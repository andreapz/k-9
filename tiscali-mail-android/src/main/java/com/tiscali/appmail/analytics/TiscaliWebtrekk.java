package com.tiscali.appmail.analytics;

import java.util.Map;

import android.app.Activity;
import android.content.Context;

/**
 * Utility class for Webtrekk Adv
 * 
 * @author Simona Figus
 *
 */
public class TiscaliWebtrekk {
    public static final String TAG_WEBTREKK_ADV = "webtrekkAdv";
    private static final String TAG_WEBTREKK_ADV_OPTED_OUT = "optedOut";
    private static final String TAG_WEBTREKK_ADV_SAMPLING_RATE = "samplingRate";
    private static final String TAG_WEBTREKK_ADV_SEND_DELAY = "sendDelay";

    /**
     * Initialise a Webtrekk session.
     * 
     * <p>
     * This will initialise a Webtrekk session. To transmit requests to Webtrekk, a session must
     * first be started.
     * </p>
     * 
     * @param context A {@link Context} instance that is used to get the string resources.
     */
    public static void startSession(Context context) {
        // Webtrekk.setContext(context);
        // Webtrekk.setServerUrl(context.getString(R.string.webtrekk_adv_url_tiscali));
        // Webtrekk.setTrackId(context.getString(R.string.webtrekk_adv_track_id_tiscali));
        // Webtrekk.setLoggingEnabled(
        // Boolean.parseBoolean(context.getString(R.string.webtrekk_adv_logging_tiscali)));//
        // optional
        // // debugging
        //
        // TiscaliConfigRemote conf =
        // new TiscaliConfigRemote((TiscaliOnTaskCompleted) new TiscaliWebtrekk(), context);
        // conf.getConfig();
        //
        // boolean isActiveWebtrekkAdv = Boolean.parseBoolean(TiscaliConfigRemote
        // .getPreferenceStringByKey(context, TiscaliWebtrekk.TAG_WEBTREKK_ADV));
        // if (!Utility.hasConnectivity(context) || !isActiveWebtrekkAdv)
        // return;
        //
        // String optedOut = TiscaliConfigRemote.getPreferenceStringByKey(context,
        // TiscaliWebtrekk.TAG_WEBTREKK_ADV_OPTED_OUT);
        // if (optedOut != null)
        // //
        // Webtrekk.setOptedOut(Boolean.parseBoolean(context.getString(R.string.webtrekk_adv_opted_out_tiscali)));
        // // optional user will not be tracked ->true | user will be tracked ->false
        // Webtrekk.setOptedOut(Boolean.parseBoolean(optedOut));
        //
        // String samplingRate = TiscaliConfigRemote.getPreferenceStringByKey(context,
        // TiscaliWebtrekk.TAG_WEBTREKK_ADV_SAMPLING_RATE);
        // if (samplingRate != null)
        // //
        // Webtrekk.setSamplingRate(Integer.parseInt(context.getString(R.string.webtrekk_adv_sampling_rate_tiscali)));
        // // optional sampling rate
        // Webtrekk.setSamplingRate(Integer.parseInt(samplingRate));
        //
        // String sendDelay = TiscaliConfigRemote.getPreferenceStringByKey(context,
        // TiscaliWebtrekk.TAG_WEBTREKK_ADV_SEND_DELAY);
        // if (sendDelay != null)
        // //
        // Webtrekk.setSendDelay(Long.parseLong(context.getString(R.string.webtrekk_adv_send_delay_tiscali)));
        // // optional default 300000 milliseconds
        // Webtrekk.setSendDelay(Long.parseLong(sendDelay));
    }

    /**
     * Start page tracking on Webtrekk.
     * 
     * <p>
     * This will start page tracking on Webtrekk.
     * </p>
     * 
     * @param className The name of the class for which to check if it is in the resources.
     * @param activity A {@link Activity} instance that is used to get the string resources and to
     *        start page tracking.
     */
    public static void startPageTracking(String className, Activity activity) {
        // Webtrekk.activityStart(activity);
        // Webtrekk.trackPage(getPageId(className, activity), getParameters(activity));
    }

    /**
     * Stop page tracking on Webtrekk.
     * 
     * <p>
     * This will stop page tracking on Webtrekk.
     * </p>
     * 
     * @param activity A {@link Activity} instance that is used to stop page tracking.
     */
    public static void stopPageTracking(Activity activity) {
    // Webtrekk.activityStop(activity);
    // }

    /**
     * Returns the value will appear in the Webtrekk analysis.
     *
     * <p>
     * This will return the value will appear in the Webtrekk analysis.
     * </p>
     *
     * @param className The name of the class for which to check if it is in the resources.
     * @param activity A {@link Activity} instance that is used to get the string resources.
     * @return The value will appear in the Webtrekk analysis.
     */
    // private static String getPageId(String className, Activity activity) {
    // int resourceId = activity.getResources().getIdentifier(className, "string",
    // activity.getPackageName());
    //
    // return resourceId == 0 ? activity.getString(R.string.webtrekk_adv_default_page_name_tiscali)
    // : activity.getString(resourceId);
    // }

    /**
     * Returns the Webtrekk parameters.
     *
     * <p>
     * This will return Webtrekk parameters.
     * </p>
     *
     * @param context A {@link Context} instance that is used to get the string resources.
     * @return The Webtrekk parameters.
     */
    private static Map<String, String> getParameters(Context context) {
        // String[] entries = context.getResources()
        // .getStringArray(R.array.webtrekk_adv_parameters_entries_tiscali);
        // String[] values = context.getResources()
        // .getStringArray(R.array.webtrekk_adv_parameters_values_tiscali);
        //
        // Map<String, String> webtrekkParams = new HashMap<String, String>();
        // for (int i = 0; i < entries.length; i++)
        // webtrekkParams.put(entries[i], values[i]);
        //
        // return webtrekkParams;
    }

}
