package com.tiscali.appmail.mail.internet;


import com.tiscali.appmail.mail.Body;


/**
 * See {@link MimeUtility#decodeBody(Body)}
 */
public interface RawDataBody extends Body {
    String getEncoding();
}
