package org.example.socketchat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class SocketChat {
    private static final String ARGUMENT_SERVER = "server";
    private static final String ARGUMENT_CLIENT = "client";

    public static void main(String... args) {
        if (args.length == 0) {
            throw new IllegalArgumentException();
        }

        switch (args[0]) {
            case ARGUMENT_SERVER:
                try (var chatServer = new ChatServer()) {
                    chatServer.acceptConnections();
                } catch (Exception e) {
                    System.exit(1);
                }

                break;
            case ARGUMENT_CLIENT:
                initClient(args);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static void initClient(String... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException();
        }

        InetAddress address;

        try {
            address = InetAddress.getByName(args[1]);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("wrong house fool");
        }

        try (var chatClient = new ChatClient(new InetSocketAddress(address, ChatServer.SERVER_PORT))) {
            chatClient.start();
            var reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            reader.lines().forEach(chatClient::sendMessage);
        } catch (Exception e) {
            System.exit(1);
        }
    }
}
