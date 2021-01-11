package com.benjaminfaal.ps4remotecontrol.ddp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Console extends DDPResponse {

    private String host;

    public String getUserFriendlyName() {
        return get("host-name") + " (" + host + ") (" + getStatus().getCode() + " " + getStatus().getText() + ")";
    }

}