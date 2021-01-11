package com.benjaminfaal.ps4remotecontrol.companionapp.packet.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Getter
public class LoginResponse extends ResponsePacket {

    private Status status;

    public LoginResponse() {
        super(Type.LOGIN);
    }

    @Override
    public void parse(ByteBuffer buffer) {
        int statusValue = buffer.getInt(8);
        this.status = Arrays.stream(Status.values())
                .filter(status -> status.value == statusValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown LoginResponse status: " + statusValue));
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    @AllArgsConstructor
    @Getter
    public enum Status {
        SUCCESS(0),
        INVALID_PIN_FORMAT(14),
        PIN_NOT_SPECIFIED(20),
        INVALID_CREDENTIAL(21),
        PASSCODE_NOT_SPECIFIED(22),
        INVALID_PIN(23),
        INVALID_PASSCODE(24),
        LOCKED(30);

        private final int value;

    }

}