package com.tiscali.appmail.helper;

import com.tiscali.appmail.K9;
import com.tiscali.appmail.R;
import com.tiscali.appmail.activity.misc.ContactPictureLoader;

import android.content.Context;
import android.util.TypedValue;

public class ContactPicture {

    public static ContactPictureLoader getContactPictureLoader(Context context) {
        final int defaultBgColor;
        if (!K9.isColorizeMissingContactPictures()) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.contactPictureFallbackDefaultBackgroundColor,
                    outValue, true);
            defaultBgColor = outValue.data;
        } else {
            defaultBgColor = 0;
        }

        return new ContactPictureLoader(context, defaultBgColor);
    }
}
