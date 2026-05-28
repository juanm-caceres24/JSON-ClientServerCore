package client;

import common.JsonPacket;
import common.ProtocolParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Independent thread running on the client side. Its sole competency is to constantly
 * listen for incoming data streams from the server without freezing the UI/Consola loop.
 */
public class ReceiverThread implements Runnable {
    private final Socket socket;
    private final ClientCore clientCore;
    private BufferedReader in;
    private boolean isListening;

    public ReceiverThread(Socket socket, ClientCore clientCore) {
        this.socket = socket;
        this.clientCore = clientCore;
        this.isListening = true;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String rawLine;
            
            // Loop blocks waiting for data from the server, running in the background.
                while (isListening && (rawLine = in.readLine()) != null) {
                    JsonPacket inboundPacket = ProtocolParser.deserialize(rawLine);
                    clientCore.handleInboundPacket(inboundPacket);
            }
        } catch (IOException e) {
            if (isListening) {
                System.err.println("[CLIENT_ERROR] Lost connection to server.");
            }
        } finally {
            clientCore.disconnect();
        }
    }

    public void stopListening() {
        this.isListening = false;
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            // Stream already closed.
        }
    }
}
