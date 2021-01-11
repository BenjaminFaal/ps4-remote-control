package com.benjaminfaal.ps4remotecontrol.companionapp.packet.response;

import lombok.Getter;

import java.nio.ByteBuffer;

@Getter
public class StartTitleResponse extends ResponsePacket {

    private int status;

    public StartTitleResponse() {
        super(Type.START_TITLE);
    }

    @Override
    public void parse(ByteBuffer buffer) {
        status = buffer.getInt(8);
    }

    public boolean isSuccess() {
        return status == 0;
    }

}