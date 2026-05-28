package server;

import common.JsonPacket;

/**
 * Interface to be implemented by any application extending this framework (e.g., a Chat or a Game).
 * It decouples the core network engine from the specific business logic.
 */
public interface ServerListener {
    /**
     * Triggered when a new client successfully establishes a connection.
     */
    void onClientConnected(ClientHandler client);

    /**
     * Triggered when a client disconnects or loses connection to the server.
     */
    void onClientDisconnected(ClientHandler client);

    /**
     * Triggered when a validated JsonPacket is received from a client.
     */
    void onPacketReceived(ClientHandler client, JsonPacket packet);
}
