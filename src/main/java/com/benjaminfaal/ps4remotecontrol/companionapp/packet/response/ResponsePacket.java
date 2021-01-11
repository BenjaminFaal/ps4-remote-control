package com.benjaminfaal.ps4remotecontrol.companionapp.packet.response;

import com.benjaminfaal.ps4remotecontrol.companionapp.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

@Getter
public abstract class ResponsePacket extends Packet<ResponsePacket.Type> {

    @Setter
    private ByteBuffer buffer;

    public ResponsePacket(ResponsePacket.Type type) {
        super(type.length, type);
    }

    public ResponsePacket(int length, ResponsePacket.Type type) {
        super(length, type);
    }

    public abstract void parse(ByteBuffer buffer);

    @Getter
    @AllArgsConstructor
    public enum Type implements Packet.Type {
        HELLO(0x6f636370, 36),
        LOGIN(7, 16),
        START_TITLE(11, 12),
        STATUS(18, 12);

        private final int value;

        private final int length;

    }

}