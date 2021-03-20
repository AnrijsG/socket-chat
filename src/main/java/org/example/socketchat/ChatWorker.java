package org.example.socketchat;

import java.net.Socket;

public abstract class ChatWorker implements AutoCloseable {
    protected final Socket socket;

    protected ChatWorker(Socket socket) {
        this.socket = socket;
    }

    protected abstract void handleConnection();
}
