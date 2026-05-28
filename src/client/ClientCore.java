package client;

import common.JsonPacket;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles the client-side network lifecycle, including establishing connection 
 * to the remote server and dispatching outbound packets.
 */
public class ClientCore {
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private ReceiverThread receiverThread;
    private SenderService senderService;
    private boolean isConnected;

    public ClientCore(String host, int port) {
        this.host = host;
        this.port = port;
        this.isConnected = false;
    }

    /**
     * Connects to the server and initializes the communication background assets.
     */
    public void connect() throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.isConnected = true;

        // Initialize outbound utility service.
        this.senderService = new SenderService(out);

        // Start the background thread to handle asynchronous inbound messages.
        this.receiverThread = new ReceiverThread(socket, this);
        new Thread(receiverThread).start();
        System.out.println("[CLIENT] Successfully connected to " + host + ":" + port);
    }

    /**
     * Routes the packet delegation down to the SenderService pipeline.
     */
    public void sendPacket(JsonPacket packet) {
        if (isConnected && senderService != null) {
            senderService.send(packet);
        }
    }

    public void disconnect() {
        if (!isConnected) return;
        isConnected = false;
        
        if (receiverThread != null) {
            receiverThread.stopListening();
        }

        try {
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("[CLIENT] Disconnected from server.");
        } catch (IOException e) {
            System.err.println("[CLIENT_ERROR] Failed to close client resources cleanly: " + e.getMessage());
        }
    }
}
