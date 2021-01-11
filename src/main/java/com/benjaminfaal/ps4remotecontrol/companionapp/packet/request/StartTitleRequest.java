package com.benjaminfaal.ps4remotecontrol.companionapp.packet.request;

public class StartTitleRequest extends RequestPacket {

    public StartTitleRequest(String titleId) {
        super(Type.START_TITLE);

        putString(titleId, 16);
    }

}