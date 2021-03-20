package org.example.socketchat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements AutoCloseable {
    private final ServerSocket serverSocket;
    private final List<Writer> writers = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService clientThreadPool = Executors.newCachedThreadPool();

    public static final int SERVER_PORT = 3000;

    public ChatServer() throws IOException {
        this.serverSocket = new ServerSocket(SERVER_PORT);
        this.serverSocket.setReuseAddress(true);
    }

    public void acceptConnections() {
        while (true) {
            try {
                var client = this.serverSocket.accept();

                this.clientThreadPool.submit(() -> {
                    try (var clientWorker = new ClientWorker(client)) {
                        clientWorker.handleConnection();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
        clientThreadPool.shutdown();
    }

    private class ClientWorker extends ChatWorker {

        private final Writer writer;

        private ClientWorker(Socket socket) {
            super(socket);
            try {
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                ChatServer.this.writers.add(writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void handleConnection() {
            try {
                var reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));

                reader.lines().forEach(line -> ChatServer.this.writers.forEach(writer -> {
                    try {
                        writer.write(line + '\n');
                        writer.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws Exception {
            ChatServer.this.writers.remove(writer);
            this.socket.close();
        }
    }
}
