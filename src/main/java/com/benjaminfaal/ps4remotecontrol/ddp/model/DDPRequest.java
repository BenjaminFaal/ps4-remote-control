package com.benjaminfaal.ps4remotecontrol.ddp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

import java.util.Map;

@Data
@NoArgsConstructor
public class DDPRequest {
    
    private MessageType type;

    @Delegate
    private Map<String, String> data;

}