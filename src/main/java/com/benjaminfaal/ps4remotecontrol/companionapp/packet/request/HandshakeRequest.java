package com.benjaminfaal.ps4remotecontrol.companionapp.packet.request;

public class HandshakeRequest extends RequestPacket {

    public HandshakeRequest(byte[] key, byte[] seed) {
        super(Type.HANDSHAKE);

        put(key);
        put(seed);
    }

}