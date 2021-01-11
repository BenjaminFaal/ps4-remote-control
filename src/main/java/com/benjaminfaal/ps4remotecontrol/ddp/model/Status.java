package com.benjaminfaal.ps4remotecontrol.ddp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

    OK(200, "Ok"),
    STANDBY(620, "Standby");

    private final int code;

    private final String text;

}