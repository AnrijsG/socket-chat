package org.example.socketchat;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient extends ChatWorker {
    private final Writer writer;

    public ChatClient(InetSocketAddress address) throws IOException {
        super(new Socket(address.getAddress(), address.getPort()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    public void start() {
        new Thread(this::handleConnection).start();
    }

    public void sendMessage(String message) {
        try {
            this.writer.write(message + '\n');
            this.writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    @Override
    protected void handleConnection() {
        try {
            var reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
            reader.lines().forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
