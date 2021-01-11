package com.benjaminfaal.ps4remotecontrol.companionapp.packet.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class RemoteControlRequest extends RequestPacket {

    public RemoteControlRequest(Operation operation, int holdTime) {
        super(Type.REMOTE_CONTROL);

        putInt(operation.value);
        putInt(holdTime);
    }

    @Getter
    @AllArgsConstructor
    public enum Operation {

        UP(1),
        DOWN(2),
        RIGHT(4),
        LEFT(8),
        ENTER(16),
        BACK(32),
        OPTIONS(64),
        PS(128),
        RELEASE(256),
        CANCEL(512),
        START(1024),
        STOP(2048);

        private final int value;

    }

}