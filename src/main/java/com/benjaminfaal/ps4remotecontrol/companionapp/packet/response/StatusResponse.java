package com.benjaminfaal.ps4remotecontrol.companionapp.packet.response;

import lombok.Getter;

import java.nio.ByteBuffer;

@Getter
public class StatusResponse extends ResponsePacket {

    private int status;

    public StatusResponse() {
        super(Type.STATUS);
    }

    @Override
    public void parse(ByteBuffer buffer) {
        status = buffer.getInt(8);
    }

}