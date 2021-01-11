package com.benjaminfaal.ps4remotecontrol.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.math.BigInteger;
import java.security.MessageDigest;

@UtilityClass
public class UserCredentialUtil {

    public static final int USER_ID_LENGTH = 19;

    public static final int HASH_LENGTH = 64;

    /**
     * Converts a PSN user ID to a SHA-256 hash used for DDP and Companion protocols as authentication.
     *
     * <p>
     * For example: "9019256736915509894" == "143f2dccb2a0d91b5473563cd77a937a18d6b14fe58060a78f95c62da0b488c8"
     * </p>
     *
     * @param userId the PSN user ID
     * @return SHA-256 of the user ID
     */
    @SneakyThrows
    public String convertUserIdToCredentialHash(String userId) {
        if (userId.length() != USER_ID_LENGTH) {
            throw new IllegalArgumentException("User ID length must be " + USER_ID_LENGTH);
        }
        return String.format("%x", new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(userId.getBytes())));
    }

}