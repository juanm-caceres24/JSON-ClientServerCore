package testchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;

import server.ServerCore;

/**
 * Executable application to launch the Chat Server.
 */
public class ChatServerApp {
    public static void main(String[] args) {
        System.out.println("Use the following IP addresses to connect clients:");
        System.out.println("Localhost: 127.0.0.1");
        String localIp = "Unavailable";
        try {     
            localIp = getLocalIp();
        } catch (SocketException e) {
            System.err.println("Failed to retrieve local IP: " + e.getMessage());
        }
        System.out.println("Local IP: " + localIp);
        String publicIp = "Unavailable";
        try {
            publicIp = getPublicIp();
        } catch (IOException e) {
            System.err.println("Failed to retrieve public IP: " + e.getMessage());
        }
        System.out.println("Public IP: " + publicIp);

        Scanner scanner = new Scanner(System.in);

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
        scanner.close();

        ChatLogic chatLogic = new ChatLogic();
        
        // Inject our specific chat logic into the generic server engine.
        ServerCore server = new ServerCore(port, chatLogic);
        
        // Start the server in a separate thread.
        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    private static String getLocalIp() throws SocketException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) continue;
            Enumeration<InetAddress> addrs = iface.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = addrs.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr.getHostAddress();
                }
            }
        }
        return "127.0.0.1";
    }

    private static String getPublicIp() throws IOException {
        URL url = URI.create("https://api.ipify.org").toURL();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return in.readLine();
        }
    }
}
