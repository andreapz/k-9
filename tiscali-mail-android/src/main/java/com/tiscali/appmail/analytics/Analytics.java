package com.tiscali.appmail.analytics;

import java.util.HashMap;
import java.util.Map;

import com.tiscali.appmail.R;

import android.app.Activity;
import android.content.Context;

/**
 * Created by andreaputzu on 17/02/17.
 */

public class Analytics {
    public static final String TAG_WEBTREKK_ADV = "webtrekkAdv";
    private static final String TAG_WEBTREKK_ADV_OPTED_OUT = "optedOut";
    private static final String TAG_WEBTREKK_ADV_SAMPLING_RATE = "samplingRate";
    private static final String TAG_WEBTREKK_ADV_SEND_DELAY = "sendDelay";

    private final Activity mActivity;

    public Analytics(Activity activity) {
        mActivity = activity;
    }

    /**
     * Returns the value will appear in the Webtrekk analysis.
     * <p>
     * <p>
     * This will return the value will appear in the Webtrekk analysis.
     * </p>
     *
     * @param className The name of the class for which to check if it is in the resources.
     * @param activity  A {@link Activity} instance that is used to get the string resources.
     * @return The value will appear in the Webtrekk analysis.
     */
    private static String getPageId(String className, Activity activity) {
        int resourceId = activity.getResources().getIdentifier(className, "string",
                activity.getPackageName());

        return resourceId == 0 ? activity.getString(R.string.webtrekk_adv_default_page_name_tiscali)
                : activity.getString(resourceId);
    }

    /**
     * Returns the Webtrekk parameters.
     * <p>
     * <p>
     * This will return Webtrekk parameters.
     * </p>
     *
     * @param context A {@link Context} instance that is used to get the string resources.
     * @return The Webtrekk parameters.
     */
    private static Map<String, String> getParameters(Context context) {
        String[] entries = context.getResources()
                .getStringArray(R.array.webtrekk_adv_parameters_entries_tiscali);
        String[] values = context.getResources()
                .getStringArray(R.array.webtrekk_adv_parameters_values_tiscali);

        Map<String, String> webtrekkParams = new HashMap<String, String>();
        for (int i = 0; i < entries.length; i++)
            webtrekkParams.put(entries[i], values[i]);

        return webtrekkParams;
    }

}
