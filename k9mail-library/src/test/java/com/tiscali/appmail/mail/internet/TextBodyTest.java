package com.tiscali.appmail.mail.internet;


import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.james.mime4j.util.MimeUtil;
import org.junit.Test;

import com.tiscali.appmail.mail.MessagingException;

import okio.Buffer;


public class TextBodyTest {
    @Test
    public void getSize_withSignUnsafeData_shouldReturnCorrectValue() throws Exception {
        TextBody textBody = new TextBody("From Bernd");
        textBody.setEncoding(MimeUtil.ENC_QUOTED_PRINTABLE);

        long result = textBody.getSize();

        int outputSize = getSizeOfSerializedBody(textBody);
        assertEquals(outputSize, result);
    }

    private int getSizeOfSerializedBody(TextBody textBody) throws IOException, MessagingException {
        Buffer buffer = new Buffer();
        textBody.writeTo(buffer.outputStream());
        return buffer.readByteString().size();
    }
}
