package com.tiscali.appmail.message.extractors;


import static com.tiscali.appmail.mail.internet.MimeUtility.isSameMimeType;

import com.tiscali.appmail.crypto.MessageDecryptVerifier;
import com.tiscali.appmail.mail.Body;
import com.tiscali.appmail.mail.BodyPart;
import com.tiscali.appmail.mail.Message;
import com.tiscali.appmail.mail.Multipart;
import com.tiscali.appmail.mail.Part;

import android.support.annotation.NonNull;


class EncryptionDetector {
    private final TextPartFinder textPartFinder;


    EncryptionDetector(TextPartFinder textPartFinder) {
        this.textPartFinder = textPartFinder;
    }

    public boolean isEncrypted(@NonNull Message message) {
        return isPgpMimeOrSMimeEncrypted(message) || containsInlinePgpEncryptedText(message);
    }

    private boolean isPgpMimeOrSMimeEncrypted(Message message) {
        return containsPartWithMimeType(message, "multipart/encrypted", "application/pkcs7-mime");
    }

    private boolean containsInlinePgpEncryptedText(Message message) {
        Part textPart = textPartFinder.findFirstTextPart(message);
        return MessageDecryptVerifier.isPartPgpInlineEncrypted(textPart);
    }

    private boolean containsPartWithMimeType(Part part, String... wantedMimeTypes) {
        String mimeType = part.getMimeType();
        if (isMimeTypeAnyOf(mimeType, wantedMimeTypes)) {
            return true;
        }

        Body body = part.getBody();
        if (body instanceof Multipart) {
            Multipart multipart = (Multipart) body;
            for (BodyPart bodyPart : multipart.getBodyParts()) {
                if (containsPartWithMimeType(bodyPart, wantedMimeTypes)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isMimeTypeAnyOf(String mimeType, String... wantedMimeTypes) {
        for (String wantedMimeType : wantedMimeTypes) {
            if (isSameMimeType(mimeType, wantedMimeType)) {
                return true;
            }
        }

        return false;
    }
}
