package com.benjaminfaal.ps4remotecontrol.companionapp.packet;

import lombok.Data;

@Data
public abstract class Packet<T extends Packet.Type> {

    private final int length;

    private final Type type;

    public interface Type {

        int getLength();

        int getValue();

    }

}