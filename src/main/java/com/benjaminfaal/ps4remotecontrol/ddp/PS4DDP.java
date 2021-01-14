package com.benjaminfaal.ps4remotecontrol.ddp;

import com.benjaminfaal.ps4remotecontrol.ddp.model.DDPRequest;
import com.benjaminfaal.ps4remotecontrol.ddp.model.DDPResponse;
import com.benjaminfaal.ps4remotecontrol.ddp.model.Console;
import com.benjaminfaal.ps4remotecontrol.ddp.model.MessageType;
import com.benjaminfaal.ps4remotecontrol.ddp.model.Status;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@UtilityClass
public class PS4DDP {

    /**
     * The UDP port on the PS4
     */
    public static final int PORT = 987;

    /**
     * Device Discovery Protocol version
     */
    private static final String DDP_VERSION = "00020020";

    public List<Console> discover(int timeout) throws IOException {
        return discover(timeout, null);
    }

    public List<Console> discover(int timeout, Consumer<Console> consoleConsumer) throws IOException {
        List<Console> consoles = new ArrayList<>();

        DatagramSocket socket = send("255.255.255.255", formatRequest(MessageType.SRCH, new HashMap<>()));
        socket.setSoTimeout(timeout);

        long started = System.currentTimeMillis();
        while (System.currentTimeMillis() - started < timeout) {
            try {
                DatagramPacket searchResponsePacket = receive(socket, 1024);
                DDPResponse searchResponse = parseResponse(new String(searchResponsePacket.getData(), 0, searchResponsePacket.getLength()));
                Console console = new Console();
                console.setHost(searchResponsePacket.getAddress().getHostAddress());
                console.setStatus(searchResponse.getStatus());
                console.setData(searchResponse.getData());
                consoles.add(console);

                if (consoleConsumer != null) {
                    consoleConsumer.accept(console);
                }
            } catch (SocketTimeoutException ignored) { }
        }
        return consoles;
    }

    public Console discover(String host, int timeout) throws IOException {
        DatagramPacket discoverPacket = sendAndReceive(host, timeout, formatRequest(MessageType.SRCH, new HashMap<>()), 1024);
        DDPResponse response = parseResponse(new String(discoverPacket.getData(), 0, discoverPacket.getLength()));
        Console console = new Console();
        console.setHost(host);
        console.setStatus(response.getStatus());
        console.setData(response.getData());
        return console;
    }

    public void wakeUp(String host, String credential) throws IOException {
        send(host, formatAuthenticatedRequest(MessageType.WAKEUP, new HashMap<>(), credential));
    }

    public void launch(String host, String credential) throws IOException {
        send(host, formatAuthenticatedRequest(MessageType.LAUNCH, new HashMap<>(), credential));
    }

    public boolean isSimulatingSupported() {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            return !socket.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public DDPRequest simulateStandby(String hostName, Function<DDPRequest, DDPResponse> responseFunction, Predicate<DDPRequest> requestPredicate) {
        return simulate(request -> {
            if (request.getType() == MessageType.SRCH) {
                DDPResponse standbyResponse = new DDPResponse();
                standbyResponse.setStatus(Status.STANDBY);
                HashMap<String, String> data = new HashMap<>();
                data.put("host-id", "1234567890AB");
                data.put("host-type", "PS4");
                data.put("host-name", hostName);
                data.put("host-request-port", "987");
                standbyResponse.setData(data);
                return standbyResponse;
            }
            return responseFunction.apply(request);
        }, requestPredicate);
    }

    public DDPRequest simulate(Function<DDPRequest, DDPResponse> responseFunction, Predicate<DDPRequest> requestPredicate) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            while (!socket.isClosed()) {
                DatagramPacket requestPacket = new DatagramPacket(new byte[2048], 2048);
                socket.receive(requestPacket);
                DDPRequest request = PS4DDP.parseRequest(new String(requestPacket.getData(), 0, requestPacket.getLength()));

                DDPResponse response = responseFunction.apply(request);
                if (response != null) {
                    String formattedResponse = PS4DDP.formatResponse(response);
                    socket.send(new DatagramPacket(formattedResponse.getBytes(), formattedResponse.length(), requestPacket.getAddress(), requestPacket.getPort()));
                }

                if (requestPredicate.test(request)) {
                    return request;
                }
            }
        } catch (BindException e) {
            String message;
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                Path javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java");
                String command = "sudo setcap 'cap_net_bind_service=ep' " + javaExecutable.toString();
                message = "Run with root access or run this command: " + System.lineSeparator() + command;
            } else {
                message = "Make sure you are allowed to open port " + PORT;
            }
            throw new IllegalStateException("Failed to bind to port " + PORT + System.lineSeparator() + message, e);
        } catch (IOException e) {
            throw new IllegalStateException("Unknown IO error: ", e);
        }
        return null;
    }

    private DatagramSocket send(String address, String message) throws IOException {
        DatagramSocket socket = new DatagramSocket(0);

        byte[] bytes = message.getBytes();
        socket.send(new DatagramPacket(bytes, bytes.length, InetAddress.getByName(address), PORT));
        return socket;
    }

    private DatagramPacket receive(DatagramSocket socket, int responseLength) throws IOException {
        DatagramPacket response = new DatagramPacket(new byte[responseLength], responseLength);
        socket.receive(response);
        return response;
    }

    private DatagramPacket sendAndReceive(String host, int timeout, String message, int responseLength) throws IOException {
        DatagramSocket socket = send(host, message);
        socket.setSoTimeout(timeout);
        return receive(socket, responseLength);
    }

    private String formatResponse(DDPResponse response) {
        return formatResponse(response.getStatus().getCode() + " " + response.getStatus().getText(), response.getData());
    }

    private String formatResponse(String status, Map<String, String> data) {
        return formatMessage("HTTP/1.1 " + status, data);
    }

    private String formatRequest(MessageType type, Map<String, String> data) {
        return formatMessage(type.name() + " * HTTP/1.1", data);
    }

    private String formatAuthenticatedRequest(MessageType type, Map<String, String> data, String credential) {
        data.put("client-type", "a");
        data.put("auth-type", "C");
        data.put("user-credential", credential);
        return formatRequest(type, data);
    }

    private String formatMessage(String status, Map<String, String> data) {
        StringBuilder message = new StringBuilder();

        message.append(status).append('\n');
        data.forEach((key, value) -> message.append(key).append(':').append(value).append('\n'));
        message.append("device-discovery-protocol-version:").append(DDP_VERSION).append('\n');

        return message.toString();
    }

    private DDPRequest parseRequest(String request) {
        MessageType messageType = Arrays.stream(MessageType.values())
                .filter(type -> request.startsWith(type.name() + " *"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported MessageType: " + request));

        DDPRequest ddpRequest = new DDPRequest();
        ddpRequest.setType(messageType);
        ddpRequest.setData(parseData(request));
        return ddpRequest;
    }

    private DDPResponse parseResponse(String response) {
        DDPResponse ddpResponse = new DDPResponse();
        ddpResponse.setStatus(parseStatus(response));
        ddpResponse.setData(parseData(response));
        return ddpResponse;
    }

    private Status parseStatus(String response) {
        String statusLine = response.substring(0, response.indexOf("\n"));
        int statusCode = Integer.parseInt(statusLine.substring(9, 12));
        String statusText = statusLine.substring(13);

        return Arrays.stream(Status.values())
                .filter(status -> status.getCode() == statusCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status code: " + statusCode + " and text " + statusText));
    }

    private Map<String, String> parseData(String message) {
        return Arrays.stream(message.split("\n"))
                .skip(1)
                .filter(line -> line.contains(":"))
                .map(line -> line.split(":", 2))
                .collect(Collectors.toMap(strings -> strings[0], strings -> strings[1]));
    }

}