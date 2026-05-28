package testchat;

import common.JsonPacket;
import server.ClientHandler;
import server.ServerListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom implementation of the ServerListener interface for a basic multi-user chat.
 * Handles broadcasting incoming messages to all active connections.
 */
public class ChatLogic implements ServerListener {
    // Keep track of all active client handlers to broadcast messages.
    private final List<ClientHandler> clients = new ArrayList<>();

    @Override
    public synchronized void onClientConnected(ClientHandler client) {
        clients.add(client);
        System.out.println("[CHAT_LOGIC] A new user connected. Total users: " + clients.size());
        
        // Optional: Notify others that someone joined.
        broadcast(new JsonPacket("SYSTEM", "A new user has joined the chat.", "Server"));
    }

    @Override
    public synchronized void onClientDisconnected(ClientHandler client) {
        clients.remove(client);
        System.out.println("[CHAT_LOGIC] A user disconnected. Total users: " + clients.size());
        broadcast(new JsonPacket("SYSTEM", "A user has left the chat.", "Server"));
    }

    @Override
    public synchronized void onPacketReceived(ClientHandler client, JsonPacket packet) {
        // Business rule: If the command is BROADCAST, broadcast it to everyone.
        if ("BROADCAST".equals(packet.getCommand())) {
            System.out.println("[CHAT_LOGIC] Processing BROADCAST command from " + packet.getSender());
            broadcast(packet);
        } else {
            System.out.println("[CHAT_LOGIC] Unknown command received: " + packet.getCommand());
        }
    }

    /**
     * Helper method to send a packet to every single connected client.
     */
    private void broadcast(JsonPacket packet) {
        for (ClientHandler client : clients) {
            client.sendPacket(packet);
        }
    }
}
