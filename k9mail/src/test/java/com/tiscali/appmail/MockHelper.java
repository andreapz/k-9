package com.tiscali.appmail;


import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class MockHelper {
    public static <T> T mockBuilder(Class<T> classToMock) {
        return mock(classToMock, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object mock = invocation.getMock();
                if (invocation.getMethod().getReturnType().isInstance(mock)) {
                    return mock;
                } else {
                    return RETURNS_DEFAULTS.answer(invocation);
                }
            }
        });
    }
}
