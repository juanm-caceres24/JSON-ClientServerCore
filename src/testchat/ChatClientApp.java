package testchat;

import client.ClientCore;
import common.JsonPacket;
import java.util.Scanner;

/**
 * Executable application to launch a Chat Client console instance.
 */
public class ChatClientApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Get IP from user input with a default fallback.
        System.out.print("Enter Server IP (default: localhost): ");
        String host = scanner.nextLine();
        if (host.trim().isEmpty()) {
            System.out.println("No IP entered. Using default IP.");
            host = "127.0.0.1";
        }
        System.out.println("Using server IP: " + host);
        
        // Get Port from user input with a default fallback.
        System.out.print("Enter Port (default: 1234): ");
        int port = 0;
        try {
            port = Integer.parseInt(scanner.nextLine());

        } catch (NumberFormatException e) {
            System.out.println("Invalid port number. Using default port.");
            port = 1234;
        }
        System.out.println("Using server port: " + port);

        // Get Username from user input.
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.println("Using username: " + username);

        ClientCore client = new ClientCore(host, port);

        try {
            // Establish TCP connection and start the background receiver thread.
            client.connect();
            
            System.out.println("<<< Chat Started >>> Type your message and press Enter. Type '/quit' to exit.");
            
            // UI Input Loop (Runs on the main thread).
            while (true) {
                String message = scanner.nextLine();
                
                // Exit condition.
                if ("/quit".equalsIgnoreCase(message.trim())) {
                    client.disconnect();
                    break;
                }
                
                if (!message.trim().isEmpty()) {
                    // Encapsulate the raw string into our framework standard packet.
                    JsonPacket packet = new JsonPacket("BROADCAST", message, username);
                    client.sendPacket(packet);
                }
            }
            
        } catch (Exception e) {
            System.err.println("[LAUNCH_ERROR] Could not connect to server: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
