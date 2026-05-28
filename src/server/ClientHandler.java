package server;

import common.JsonPacket;
import common.ProtocolParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Manages the persistent network connection for a single connected client.
 * Runs on a dedicated thread to perform non-blocking I/O operations.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ServerListener listener;
    private final ServerCore serverCore;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected;

    public ClientHandler(Socket socket, ServerListener listener, ServerCore serverCore) {
        this.socket = socket;
        this.listener = listener;
        this.serverCore = serverCore;
        this.isConnected = true;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);  // Auto-flush enabled.

            String rawLine;
            // Read incoming JSON string streams terminated by a newline character (\n).
            while (isConnected && (rawLine = in.readLine()) != null) {
                JsonPacket packet = ProtocolParser.deserialize(rawLine);
                listener.onPacketReceived(this, packet);
            }
        } catch (IOException e) {
            // Connection lost or closed unexpectedly.
        } finally {
            disconnect();
        }
    }

    /**
     * Sends a JsonPacket to this specific client.
     */
    public void sendPacket(JsonPacket packet) {
        if (out != null && isConnected) {
            String json = ProtocolParser.serialize(packet);
            out.println(json);  // Sends the string with the crucial \n delimiter.
        }
    }

    public void disconnect() {
        if (!isConnected) return;
        
        isConnected = false;
        serverCore.removeClient(this);
        
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[SERVER_ERROR] Error closing client resources: " + e.getMessage());
        }
    }
}
