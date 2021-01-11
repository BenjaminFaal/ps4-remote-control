package com.benjaminfaal.ps4remotecontrol.companionapp.packet.response;

import lombok.Getter;

import java.nio.ByteBuffer;

@Getter
public class HelloResponse extends ResponsePacket {

    private int version;

    private byte[] padding = new byte[8];

    private byte[] seed = new byte[16];

    public HelloResponse() {
        super(Type.HELLO);
    }

    @Override
    public void parse(ByteBuffer buffer) {
        version = buffer.getInt();
        buffer.get(padding);
        buffer.get(seed);
    }
}