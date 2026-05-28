package client;

import common.JsonPacket;

/**
 * Callback for applications that want to handle inbound packets from the server.
 */
public interface ClientPacketListener {
    void onPacketReceived(JsonPacket packet);
}
