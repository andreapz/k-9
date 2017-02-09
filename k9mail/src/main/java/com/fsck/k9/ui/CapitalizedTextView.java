package com.fsck.k9.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Annalisa Sini on 09/02/2017.
 */

public class CapitalizedTextView extends TextView {

    public CapitalizedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // all setText(params) are final except this
    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text.length() > 0) {
            text = String.valueOf(text.charAt(0)).toUpperCase()
                    + text.subSequence(1, text.length());
        }
        super.setText(text, type);
    }
}
