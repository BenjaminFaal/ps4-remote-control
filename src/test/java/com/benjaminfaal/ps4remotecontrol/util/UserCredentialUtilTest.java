package com.benjaminfaal.ps4remotecontrol.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserCredentialUtilTest {

    @Test
    public void convertUserIdToCredentialsHash() {
        assertEquals(UserCredentialUtil.convertUserIdToCredentialHash("9019256736915509894"), "143f2dccb2a0d91b5473563cd77a937a18d6b14fe58060a78f95c62da0b488c8");
    }

}