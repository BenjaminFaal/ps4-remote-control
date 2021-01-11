package com.benjaminfaal.ps4remotecontrol.companionapp;

import com.benjaminfaal.ps4remotecontrol.companionapp.packet.request.ByeRequest;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.request.HandshakeRequest;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.request.HelloRequest;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.request.LoginRequest;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.request.LogoutRequest;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.request.RemoteControlRequest;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.request.RequestPacket;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.request.StartTitleRequest;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.request.StatusRequest;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.response.HelloResponse;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.response.LoginResponse;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.response.ResponsePacket;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.response.StartTitleResponse;
import com.benjaminfaal.ps4remotecontrol.companionapp.packet.response.StatusResponse;
import lombok.Getter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class PS4CompanionAppConnection {

    private static final int PORT = 997;

    /**
     * https://www.psdevwiki.com/ps4/Keys#Companion_App_Protocol_RSA_Public_Key
     */
    private static final String RSA_PUBLIC_KEY =
            "-----BEGIN PUBLIC KEY-----\n" +
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxfAO/MDk5ovZpp7xlG9J\n" +
                    "JKc4Sg4ztAz+BbOt6Gbhub02tF9bryklpTIyzM0v817pwQ3TCoigpxEcWdTykhDL\n" +
                    "cGhAbcp6E7Xh8aHEsqgtQ/c+wY1zIl3fU//uddlB1XuipXthDv6emXsyyU/tJWqc\n" +
                    "zy9HCJncLJeYo7MJvf2TE9nnlVm1x4flmD0k1zrvb3MONqoZbKb/TQVuVhBv7SM+\n" +
                    "U5PSi3diXIx1Nnj4vQ8clRNUJ5X1tT9XfVmKQS1J513XNZ0uYHYRDzQYujpLWucu\n" +
                    "ob7v50wCpUm3iKP1fYCixMP6xFm0jPYz1YQaMV35VkYwc40qgk3av0PDS+1G0dCm\n" +
                    "swIDAQAB\n" +
                    "-----END PUBLIC KEY-----";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Getter
    private final String host;

    private final String credential;

    private Socket socket;

    private Cipher cipher;

    private Cipher decipher;

    @Getter
    private boolean loggedIn;

    public PS4CompanionAppConnection(String host, String credential) {
        this.host = host;
        this.credential = credential;
    }

    private static PublicKey createRSAPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKey = RSA_PUBLIC_KEY
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("\\n", "")
                .replace("-----END PUBLIC KEY-----", "");
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
    }

    public void connect() throws IOException, GeneralSecurityException {
        socket = new Socket(host, PORT);

        send(new HelloRequest(), false);
        HelloResponse helloResponse = receive(new HelloResponse(), false);

        SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(helloResponse.getSeed());

        cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        decipher = Cipher.getInstance("AES/CBC/NoPadding");
        decipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        Cipher publicCipher = Cipher.getInstance("RSA/PKCS1/OAEPPadding", BouncyCastleProvider.PROVIDER_NAME);
        publicCipher.init(Cipher.ENCRYPT_MODE, createRSAPublicKey());
        byte[] key = publicCipher.doFinal(secretKey.getEncoded());

        HandshakeRequest handshakeRequest = new HandshakeRequest(key, helloResponse.getSeed());
        send(handshakeRequest, false);
    }

    public LoginResponse login(String pincode, String passcode, String appLabel, String deviceName) throws IOException {
        LoginRequest loginRequest = new LoginRequest(passcode, credential, appLabel, "4.4", deviceName, pincode);
        send(loginRequest, true);
        LoginResponse loginResponse = receive(new LoginResponse(), true);
        loggedIn = loginResponse.isSuccess();
        return loginResponse;
    }

    public void logout() throws IOException {
        if (!loggedIn) {
            throw new IllegalStateException("Not logged in");
        }
        send(new LogoutRequest(), true);
    }

    public void sendStatus(int status) throws IOException {
        send(new StatusRequest(status), true);
    }

    public StatusResponse receiveStatus() throws IOException {
        return receive(new StatusResponse(), true);
    }

    public StartTitleResponse startTitle(String titleId) throws IOException {
        send(new StartTitleRequest(titleId), true);
        return receive(new StartTitleResponse(), true);
    }

    public void remoteControl(RemoteControlRequest.Operation operation, int holdTime) throws IOException {
        send(new RemoteControlRequest(operation, holdTime), true);
    }

    public void disconnect() throws IOException {
        if (socket == null) {
            throw new IllegalStateException("Not connected.");
        } else if (!socket.isConnected()) {
            throw new IllegalStateException("Already disconnected.");
        }
        if (loggedIn) {
            logout();
        }

        send(new ByeRequest(), true);
        socket.close();
        socket = null;
        cipher = null;
        decipher = null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void send(RequestPacket request, boolean encrypted) throws IOException {
        OutputStream outputStream = encrypted ? new CipherOutputStream(socket.getOutputStream(), cipher) : socket.getOutputStream();
        byte[] bytes = request.array();

        if (encrypted) {
            int lengthWithPadding = 1 + (bytes.length - 1) / 16 << 4;
            if (lengthWithPadding != bytes.length) {
                bytes = Arrays.copyOf(bytes, lengthWithPadding);
            }
        }

        outputStream.write(bytes);
    }

    public <R extends ResponsePacket> R receive(R response, boolean decrypt) throws IOException {
        InputStream inputStream = decrypt ? new CipherInputStream(socket.getInputStream(), decipher) : socket.getInputStream();

        byte[] bytes = new byte[response.getLength()];

        int read = inputStream.read(bytes);
        if (read != response.getLength()) {
            throw new IllegalStateException("Invalid response length: " + read + " expected: " + response.getLength());
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        response.setBuffer(buffer.asReadOnlyBuffer());

        int length = buffer.getInt();
        int type = buffer.getInt();
        if (type != response.getType().getValue()) {
            // ignore status received and retry to receive the correct response
            if (type == ResponsePacket.Type.STATUS.getValue()) {
                return receive(response, decrypt);
            }
            throw new IllegalStateException("Unexpected response type: " + type + " expected: " + response.getType() + "(" + response.getType().getValue() + ")");
        } else if (length != response.getLength()) {
            throw new IllegalStateException("Unexpected response length: " + length + " expected: " + response.getLength());
        }

        response.parse(buffer);

        return response;
    }

}