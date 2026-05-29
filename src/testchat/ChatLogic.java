package testchat;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.JsonPacket;
import server.ClientHandler;
import server.ServerListener;

/**
 * Custom implementation of the ServerListener interface for a basic multi-user chat.
 * Handles broadcasting incoming messages to all active connections.
 */
public class ChatLogic implements ServerListener {
    // Keep track of all active client handlers to broadcast messages.
    private final List<ClientHandler> clients = new ArrayList<>();
    private final Map<ClientHandler, String> usernamesByClient = new HashMap<>();
    private final Map<String, ClientHandler> clientsByUsername = new HashMap<>();

    @Override
    public synchronized void onClientConnected(ClientHandler client) {
        clients.add(client);
        System.out.println("[CHAT_LOGIC] A new user connected. Total users: " + clients.size());
    }

    @Override
    public synchronized void onClientDisconnected(ClientHandler client) {
        String username = usernamesByClient.remove(client);
        if (username != null) {
            clientsByUsername.remove(username);
            broadcast(new JsonPacket("SYSTEM", username + " has left the chat.", "Server"));
        }

        clients.remove(client);
        System.out.println("[CHAT_LOGIC] A user disconnected. Total users: " + clients.size());
    }

    @Override
    public synchronized void onPacketReceived(ClientHandler client, JsonPacket packet) {
        String command = packet.getCommand();

        if ("REGISTER".equals(command)) {
            registerClient(client, packet.getSender());
            return;
        }

        if (!isRegistered(client)) {
            registerClient(client, packet.getSender());
        }

        if ("BROADCAST".equals(command)) {
            System.out.println("[CHAT_LOGIC] Processing BROADCAST command from " + packet.getSender());
            broadcast(packet);
        } else if ("LIST_USERS".equals(command)) {
            System.out.println("[CHAT_LOGIC] Processing LIST_USERS command from " + packet.getSender());
            client.sendPacket(new JsonPacket("USERS_LIST", buildUserList(), "Server"));
        } else if ("PRIVATE".equals(command)) {
            System.out.println("[CHAT_LOGIC] Processing PRIVATE command from " + packet.getSender());
            handlePrivateMessage(client, packet);
        } else {
            System.out.println("[CHAT_LOGIC] Unknown command received: " + command);
            client.sendPacket(new JsonPacket("ERROR", "Unknown command: " + command, "Server"));
        }
    }

    /**
     * Helper method to send a packet to every single connected client.
     */
    private void broadcast(JsonPacket packet) {
        for (ClientHandler client : new ArrayList<>(clients)) {
            client.sendPacket(packet);
        }
    }

    private void registerClient(ClientHandler client, String username) {
        String normalizedUsername = username == null ? "" : username.trim();

        if (normalizedUsername.isEmpty()) {
            client.sendPacket(new JsonPacket("ERROR", "Username cannot be empty.", "Server"));
            return;
        }

        String currentUsername = usernamesByClient.get(client);
        if (normalizedUsername.equals(currentUsername)) {
            return;
        }

        ClientHandler existingClient = clientsByUsername.get(normalizedUsername);
        if (existingClient != null && existingClient != client) {
            client.sendPacket(new JsonPacket("ERROR", "Username already in use: " + normalizedUsername, "Server"));
            client.disconnect();
            return;
        }

        if (currentUsername != null) {
            clientsByUsername.remove(currentUsername);
        }

        usernamesByClient.put(client, normalizedUsername);
        clientsByUsername.put(normalizedUsername, client);
        client.sendPacket(new JsonPacket("SYSTEM", "Registered as " + normalizedUsername, "Server"));
        broadcast(new JsonPacket("SYSTEM", normalizedUsername + " joined the chat.", "Server"));
    }

    private boolean isRegistered(ClientHandler client) {
        return usernamesByClient.containsKey(client);
    }

    private String buildUserList() {
        if (clientsByUsername.isEmpty()) {
            return "No active users.";
        }

        StringBuilder builder = new StringBuilder("Active users:\n");
        for (String username : clientsByUsername.keySet()) {
            builder.append("- ").append(username).append('\n');
        }
        return builder.toString().trim();
    }

    private void handlePrivateMessage(ClientHandler senderClient, JsonPacket packet) {
        String senderUsername = usernamesByClient.get(senderClient);
        String content = packet.getContent() == null ? "" : packet.getContent();
        String[] parts = content.split("\\|", 2);

        if (parts.length < 2) {
            senderClient.sendPacket(new JsonPacket("ERROR", "Private messages must use the format target|message.", "Server"));
            return;
        }

        String targetUsername = parts[0].trim();
        String message = parts[1].trim();

        if (targetUsername.isEmpty() || message.isEmpty()) {
            senderClient.sendPacket(new JsonPacket("ERROR", "Private messages require a target user and a message.", "Server"));
            return;
        }

        ClientHandler targetClient = clientsByUsername.get(targetUsername);
        if (targetClient == null) {
            senderClient.sendPacket(new JsonPacket("ERROR", "User not found: " + targetUsername, "Server"));
            return;
        }

        JsonPacket privatePacket = new JsonPacket("PRIVATE_MESSAGE", message, senderUsername);
        targetClient.sendPacket(privatePacket);
        senderClient.sendPacket(new JsonPacket("PRIVATE_SENT", message, targetUsername));
    }
}
