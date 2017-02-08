package com.tiscali.appmail.mail.store.imap;


import java.io.IOException;
import java.util.Map;

import com.tiscali.appmail.mail.Message;
import com.tiscali.appmail.mail.MessagingException;
import com.tiscali.appmail.mail.filter.FixedLengthInputStream;


class FetchBodyCallback implements ImapResponseCallback {
    private Map<String, Message> mMessageMap;

    FetchBodyCallback(Map<String, Message> messageMap) {
        mMessageMap = messageMap;
    }

    @Override
    public Object foundLiteral(ImapResponse response, FixedLengthInputStream literal)
            throws MessagingException, IOException {
        if (response.getTag() == null
                && ImapResponseParser.equalsIgnoreCase(response.get(1), "FETCH")) {
            ImapList fetchList = (ImapList) response.getKeyedValue("FETCH");
            String uid = fetchList.getKeyedString("UID");

            ImapMessage message = (ImapMessage) mMessageMap.get(uid);
            message.parse(literal);

            // Return placeholder object
            return 1;
        }
        return null;
    }
}
