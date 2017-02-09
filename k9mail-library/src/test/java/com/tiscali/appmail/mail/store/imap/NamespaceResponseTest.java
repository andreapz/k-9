package com.tiscali.appmail.mail.store.imap;


import static com.tiscali.appmail.mail.store.imap.ImapResponseHelper.createImapResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;


public class NamespaceResponseTest {

    @Test
    public void parse_withProperNamespaceResponse() throws Exception {
        NamespaceResponse result = parse("* NAMESPACE ((\"\" \"/\")) NIL NIL");

        assertNotNull(result);
        assertEquals("", result.getPrefix());
        assertEquals("/", result.getHierarchyDelimiter());
    }

    @Test
    public void parse_withoutNamespaceResponse_shouldReturnNull() throws Exception {
        NamespaceResponse result = parse("* OK Some text here");

        assertNull(result);
    }

    @Test
    public void parse_withTooShortNamespaceResponse_shouldReturnNull() throws Exception {
        NamespaceResponse result = parse("* NAMESPACE NIL NIL");

        assertNull(result);
    }

    @Test
    public void parse_withPersonalNamespacesNotPresent_shouldReturnNull() throws Exception {
        NamespaceResponse result = parse("* NAMESPACE NIL NIL NIL");

        assertNull(result);
    }

    @Test
    public void parse_withEmptyListForPersonalNamespaces_shouldReturnNull() throws Exception {
        NamespaceResponse result = parse("* NAMESPACE () NIL NIL");

        assertNull(result);
    }

    @Test
    public void parse_withEmptyListForFirstPersonalNamespace_shouldReturnNull() throws Exception {
        NamespaceResponse result = parse("* NAMESPACE (()) NIL NIL");

        assertNull(result);
    }

    @Test
    public void parse_withIncompleteFirstPersonalNamespace_shouldReturnNull() throws Exception {
        NamespaceResponse result = parse("* NAMESPACE ((\"\")) NIL NIL");

        assertNull(result);
    }

    @Test
    public void parse_withEmptyResponseList() throws Exception {
        NamespaceResponse result = NamespaceResponse.parse(Collections.<ImapResponse>emptyList());

        assertNull(result);
    }

    @Test
    public void parse_withSingleItemInResponseList() throws Exception {
        ImapResponse imapResponse = createImapResponse("* NAMESPACE ((\"\" \"/\")) NIL NIL");
        NamespaceResponse result = NamespaceResponse.parse(Collections.singletonList(imapResponse));

        assertNotNull(result);
        assertEquals("", result.getPrefix());
        assertEquals("/", result.getHierarchyDelimiter());
    }

    @Test
    public void parse_withResponseList() throws Exception {
        ImapResponse imapResponseOne = createImapResponse("* OK");
        ImapResponse imapResponseTwo =
                createImapResponse("* NAMESPACE ((\"INBOX\" \".\")) NIL NIL");
        NamespaceResponse result =
                NamespaceResponse.parse(Arrays.asList(imapResponseOne, imapResponseTwo));

        assertNotNull(result);
        assertEquals("INBOX", result.getPrefix());
        assertEquals(".", result.getHierarchyDelimiter());
    }


    private NamespaceResponse parse(String response) throws IOException {
        ImapResponse imapResponse = createImapResponse(response);

        return NamespaceResponse.parse(imapResponse);
    }
}
