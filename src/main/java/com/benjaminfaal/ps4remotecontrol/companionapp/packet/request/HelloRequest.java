package com.benjaminfaal.ps4remotecontrol.companionapp.packet.request;

public class HelloRequest extends RequestPacket {

    private static final int VERSION = 0x20000;

    public HelloRequest() {
        super(Type.HELLO);

        putInt(VERSION);
        putInt(0);
    }

}