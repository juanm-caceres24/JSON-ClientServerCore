package testchat;

import server.ServerCore;

/**
 * Executable application to launch the Chat Server.
 */
public class ChatServerApp {
    public static void main(String[] args) {
        int port = 1234;
        ChatLogic chatLogic = new ChatLogic();
        
        // Inject our specific chat logic into the generic server engine.
        ServerCore server = new ServerCore(port, chatLogic);
        
        // Start the server in a separate thread.
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}
