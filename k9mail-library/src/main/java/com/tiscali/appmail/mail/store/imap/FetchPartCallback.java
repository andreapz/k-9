package com.tiscali.appmail.mail.store.imap;


import java.io.IOException;

import com.tiscali.appmail.mail.Part;
import com.tiscali.appmail.mail.filter.FixedLengthInputStream;
import com.tiscali.appmail.mail.internet.MimeHeader;
import com.tiscali.appmail.mail.internet.MimeUtility;


class FetchPartCallback implements ImapResponseCallback {
    private Part mPart;

    FetchPartCallback(Part part) {
        mPart = part;
    }

    @Override
    public Object foundLiteral(ImapResponse response, FixedLengthInputStream literal)
            throws IOException {
        if (response.getTag() == null
                && ImapResponseParser.equalsIgnoreCase(response.get(1), "FETCH")) {
            // TODO: check for correct UID

            String contentTransferEncoding =
                    mPart.getHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING)[0];
            String contentType = mPart.getHeader(MimeHeader.HEADER_CONTENT_TYPE)[0];

            return MimeUtility.createBody(literal, contentTransferEncoding, contentType);
        }
        return null;
    }
}
