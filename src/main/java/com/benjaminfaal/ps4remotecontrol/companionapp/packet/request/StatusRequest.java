package com.benjaminfaal.ps4remotecontrol.companionapp.packet.request;

public class StatusRequest extends RequestPacket {

    public StatusRequest(int status) {
        super(Type.STATUS);

        putInt(status);
    }

}