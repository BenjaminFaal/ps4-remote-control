# ps4-remote-control
Java library for remote controlling a PS4 with the DDP and Companion app protocols.

[![Release](https://jitpack.io/v/BenjaminFaal/ps4-remote-control.svg)](https://jitpack.io/#BenjaminFaal/ps4-remote-control)
## Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
	<groupId>com.github.BenjaminFaal</groupId>
	<artifactId>ps4-remote-control</artifactId>
	<version>0.0.1</version>
</dependency>
```

## Usage

```java
import com.benjaminfaal.ps4remotecontrol.companionapp.PS4CompanionAppConnection;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.request.RemoteControlRequest;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.response.LoginResponse;
import com.benjaminfaal.ps4remotecontrol.ddp.PS4DDP;
import com.benjaminfaal.ps4remotecontrol.ddp.model.Console;
import com.benjaminfaal.ps4remotecontrol.util.UserCredentialUtil;

import java.util.List;

public class PS4RemoteControlTest {

    public static void main(String[] args) throws Exception {
        // discover consoles
        List<Console> consoles = PS4DDP.discover(1000);

        String host = consoles.get(0).getHost();
        String credential = UserCredentialUtil.convertUserIdToCredentialHash("1234567891234567891");
        // wakeup the console and launch it so it will accept incoming requests on the companion app protocol
        PS4DDP.wakeUp(host, credential);
        PS4DDP.launch(host, credential);

        PS4CompanionAppConnection connection = new PS4CompanionAppConnection(host, credential);
        connection.connect();

        LoginResponse loginResponse = connection.login("12345678", "", "App label here", "Device name here");
        if (loginResponse.isSuccess()) {
            connection.startTitle("CUSA00001");
            connection.remoteControl(RemoteControlRequest.Operation.UP, 0);
        }

        connection.disconnect();
    }

}
```