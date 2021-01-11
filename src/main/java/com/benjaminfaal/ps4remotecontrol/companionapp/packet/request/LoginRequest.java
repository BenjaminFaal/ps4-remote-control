package com.benjaminfaal.ps4remotecontrol.companionapp.packet.request;

public class LoginRequest extends RequestPacket {

    public LoginRequest(String passcode, String credential, String appLabel, String osVersion, String model, String pincode) {
        super(Type.LOGIN);

        putString(passcode, 4);
        putInt(0x201);
        putString(credential, 64);
        putString(appLabel, 256);
        putString(osVersion, 16);
        putString(model, 16);
        putString(pincode, 16);
    }

}