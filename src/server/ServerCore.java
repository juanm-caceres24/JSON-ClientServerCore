package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The main server engine. It initializes the ServerSocket on a specific port
 * and runs a continuous loop to accept incoming client connections.
 */
public class ServerCore implements Runnable {
    private final int port;
    private final ServerListener listener;
    private final List<ClientHandler> activeClients;
    private ServerSocket serverSocket;
    private boolean isRunning;

    public ServerCore(int port, ServerListener listener) {
        this.port = port;
        this.listener = listener;
        this.activeClients = new ArrayList<>();
        this.isRunning = false;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("[SERVER] Core started on port " + port + ". Awaiting connections...");

            while (isRunning) {
                // Blocks until a new client connects (TCP Handshake).
                Socket clientSocket = serverSocket.accept(); 
                
                // Spawns a dedicated handler and thread for the connected client.
                ClientHandler handler = new ClientHandler(clientSocket, listener, this);
                synchronized (activeClients) {
                    activeClients.add(handler);
                }
                
                // Notify the custom business logic layer.
                listener.onClientConnected(handler);
                
                // Start listening to this client in a background thread.
                new Thread(handler).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("[SERVER_ERROR] Critical failure in server loop: " + e.getMessage());
            }
        }
    }

    public void removeClient(ClientHandler handler) {
        synchronized (activeClients) {
            activeClients.remove(handler);
        }
        listener.onClientDisconnected(handler);
    }

    public void stop() {
        this.isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[SERVER_ERROR] Error closing server socket: " + e.getMessage());
        }
    }
}
