
package com.tiscali.appmail;

import java.util.regex.Pattern;

import android.text.util.Rfc822Tokenizer;
import android.widget.AutoCompleteTextView.Validator;

public class EmailAddressValidator implements Validator {
    private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "("
                    + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

    public CharSequence fixText(CharSequence invalidText) {
        return "";
    }

    public boolean isValid(CharSequence text) {
        return Rfc822Tokenizer.tokenize(text).length > 0;
    }

    public boolean isValidAddressOnly(CharSequence text) {
        return EMAIL_ADDRESS_PATTERN.matcher(text).matches();
    }
}
