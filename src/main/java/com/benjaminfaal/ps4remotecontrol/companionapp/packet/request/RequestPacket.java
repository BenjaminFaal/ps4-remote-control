package com.benjaminfaal.ps4remotecontrol.companionapp.packet.request;

import com.benjaminfaal.ps4remotecontrol.companionapp.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public abstract class RequestPacket extends Packet<RequestPacket.Type> {

    @Delegate
    protected final ByteBuffer buffer;

    public RequestPacket(Type type) {
        this(type.length, type);
    }

    public RequestPacket(int length, Type type) {
        super(length, type);
        buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(length);
        buffer.putInt(type.value);
    }

    public Packet putString(String s, int maxLength) {
        if (s.length() == maxLength) {
            put(s.getBytes());
            return this;
        }
        String result = s;
        if (s.length() > maxLength) {
            result = s.substring(0, maxLength - 1);
        }
        char[] padding = new char[maxLength - result.length()];
        if (padding.length > 0) {
            Arrays.fill(padding, (char) 0x00);
            result += new String(padding);
        }
        put(result.getBytes());
        return this;
    }

    @Getter
    @AllArgsConstructor
    public enum Type implements Packet.Type {
        BYE(4, 8),
        HANDSHAKE(32, 280),
        HELLO(0x6f636370, 28),
        LOGIN(30, 384),
        LOGOUT(34, 8),
        START_TITLE(10, 24),
        OSK_CHANGE_STRING(14, -1),
        OSK_CONTROL(16, 12),
        OSK_START(12, 8),
        REMOTE_CONTROL(28, 16),
        STATUS(20, 12);

        private final int value;

        private final int length;

    }

}