package com.tiscali.appmail.message;


import com.tiscali.appmail.mail.Body;
import com.tiscali.appmail.mail.BodyPart;
import com.tiscali.appmail.mail.Message;
import com.tiscali.appmail.mail.MessagingException;
import com.tiscali.appmail.mail.internet.MimeBodyPart;
import com.tiscali.appmail.mail.internet.MimeHeader;
import com.tiscali.appmail.mail.internet.MimeMessage;
import com.tiscali.appmail.mail.internet.MimeMultipart;
import com.tiscali.appmail.mail.internet.TextBody;
import com.tiscali.appmail.mailstore.BinaryMemoryBody;


public class MessageCreationHelper {
    public static BodyPart createTextPart(String mimeType) throws MessagingException {
        return createTextPart(mimeType, "");
    }

    public static BodyPart createTextPart(String mimeType, String text) throws MessagingException {
        TextBody body = new TextBody(text);
        return new MimeBodyPart(body, mimeType);
    }

    public static BodyPart createEmptyPart(String mimeType) throws MessagingException {
        return new MimeBodyPart(null, mimeType);
    }

    public static BodyPart createPart(String mimeType) throws MessagingException {
        BinaryMemoryBody body = new BinaryMemoryBody(new byte[0], "utf-8");
        return new MimeBodyPart(body, mimeType);
    }

    public static BodyPart createMultipart(String mimeType, BodyPart... parts)
            throws MessagingException {
        MimeMultipart multipart = createMultipartBody(mimeType, parts);
        return new MimeBodyPart(multipart, mimeType);
    }

    public static Message createTextMessage(String mimeType, String text)
            throws MessagingException {
        TextBody body = new TextBody(text);
        return createMessage(mimeType, body);
    }

    public static Message createMultipartMessage(String mimeType, BodyPart... parts)
            throws MessagingException {
        MimeMultipart body = createMultipartBody(mimeType, parts);
        return createMessage(mimeType, body);
    }

    public static Message createMessage(String mimeType) throws MessagingException {
        return createMessage(mimeType, null);
    }

    private static Message createMessage(String mimeType, Body body) throws MessagingException {
        MimeMessage message = new MimeMessage();
        message.setBody(body);
        message.setHeader(MimeHeader.HEADER_CONTENT_TYPE, mimeType);

        return message;
    }

    private static MimeMultipart createMultipartBody(String mimeType, BodyPart[] parts)
            throws MessagingException {
        MimeMultipart multipart = new MimeMultipart(mimeType, "boundary");
        for (BodyPart part : parts) {
            multipart.addBodyPart(part);
        }
        return multipart;
    }
}
