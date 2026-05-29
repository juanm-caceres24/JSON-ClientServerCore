package client;

import java.io.PrintWriter;

import common.JsonPacket;
import common.ProtocolParser;

/**
 * Dedicated utility service handling outbound packet transmissions.
 * Encapsulates the serialization logic to shield ClientCore from structural parsing steps.
 */
public class SenderService {
    private final PrintWriter out;

    public SenderService(PrintWriter out) {
        this.out = out;
    }

    /**
     * Translates a packet into a standardized JSON text stream and pushes it down the socket pipeline.
     */
    public synchronized void send(JsonPacket packet) {
        if (out != null) {
            String jsonStr = ProtocolParser.serialize(packet);
            out.println(jsonStr);   // Append the required \n boundary and flush.
        }
    }
}
