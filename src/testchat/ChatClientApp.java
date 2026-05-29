package testchat;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import client.ClientCore;
import client.ClientPacketListener;
import common.JsonPacket;

/**
 * Executable application to launch a Chat Client console instance.
 */
public class ChatClientApp {
    private static final Object CONSOLE_LOCK = new Object();
    private static final List<String> HISTORY = new ArrayList<>();

    private enum ClientMode {
        MAIN_MENU,
        BROADCAST,
        PRIVATE_TARGET,
        PRIVATE_CHAT
    }

    private static ClientMode currentMode = ClientMode.MAIN_MENU;
    private static String privateTarget = null;
    private static boolean isRunning = true;

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
        client.setPacketListener(new ClientPacketListener() {
            @Override
            public void onPacketReceived(JsonPacket packet) {
                handleInboundPacket(packet);
            }
        });

        try {
            // Establish TCP connection and start the background receiver thread.
            client.connect();

            client.sendPacket(new JsonPacket("REGISTER", username, username));
            synchronized (CONSOLE_LOCK) {
                HISTORY.add("[SYSTEM] Connected to server as " + username);
                renderConsole();
            }

            // UI Input Loop (Runs on the main thread).
            while (isRunning) {
                String input = scanner.nextLine();
                String trimmedInput = input.trim();

                if ("/quit".equalsIgnoreCase(trimmedInput)) {
                    client.disconnect();
                    isRunning = false;
                    break;
                }

                JsonPacket outboundPacket = null;
                boolean refreshConsole = false;

                synchronized (CONSOLE_LOCK) {
                    switch (currentMode) {
                        case MAIN_MENU:
                            if ("1".equals(trimmedInput)) {
                                currentMode = ClientMode.BROADCAST;
                                refreshConsole = true;
                            } else if ("2".equals(trimmedInput)) {
                                outboundPacket = new JsonPacket("LIST_USERS", "", username);
                                refreshConsole = true;
                            } else if ("3".equals(trimmedInput)) {
                                currentMode = ClientMode.PRIVATE_TARGET;
                                refreshConsole = true;
                            } else if ("4".equals(trimmedInput)) {
                                client.disconnect();
                                isRunning = false;
                            } else {
                                HISTORY.add("[SYSTEM] Invalid option. Choose 1, 2, 3 or 4.");
                                refreshConsole = true;
                            }
                            break;

                        case BROADCAST:
                            if ("/back".equalsIgnoreCase(trimmedInput)) {
                                currentMode = ClientMode.MAIN_MENU;
                                refreshConsole = true;
                            } else if (!trimmedInput.isEmpty()) {
                                outboundPacket = new JsonPacket("BROADCAST", input, username);
                            }
                            break;

                        case PRIVATE_TARGET:
                            if ("/back".equalsIgnoreCase(trimmedInput)) {
                                currentMode = ClientMode.MAIN_MENU;
                                refreshConsole = true;
                            } else if (!trimmedInput.isEmpty()) {
                                privateTarget = trimmedInput;
                                currentMode = ClientMode.PRIVATE_CHAT;
                                refreshConsole = true;
                            }
                            break;

                        case PRIVATE_CHAT:
                            if ("/back".equalsIgnoreCase(trimmedInput)) {
                                privateTarget = null;
                                currentMode = ClientMode.MAIN_MENU;
                                refreshConsole = true;
                            } else if (!trimmedInput.isEmpty()) {
                                outboundPacket = new JsonPacket("PRIVATE", privateTarget + "|" + input, username);
                            }
                            break;
                        default:
                            break;
                    }

                    if (refreshConsole) {
                        renderConsole();
                    }
                }

                if (outboundPacket != null) {
                    client.sendPacket(outboundPacket);
                }
            }
            
        } catch (Exception e) {
            System.err.println("[LAUNCH_ERROR] Could not connect to server: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void handleInboundPacket(JsonPacket packet) {
        synchronized (CONSOLE_LOCK) {
            HISTORY.add(formatPacket(packet));
            renderConsole();
        }
    }

    private static String formatPacket(JsonPacket packet) {
        String command = packet.getCommand();

        if ("SYSTEM".equals(command)) {
            return "[SYSTEM] " + packet.getContent();
        } else if ("USERS_LIST".equals(command)) {
            return "[USERS]" + System.lineSeparator() + packet.getContent();
        } else if ("PRIVATE_MESSAGE".equals(command)) {
            return "[PRIVATE] from " + packet.getSender() + ": " + packet.getContent();
        } else if ("PRIVATE_SENT".equals(command)) {
            return "[PRIVATE] to " + packet.getSender() + ": " + packet.getContent();
        } else if ("ERROR".equals(command)) {
            return "[ERROR] " + packet.getContent();
        } else {
            return "[" + command + "] " + packet.getSender() + ": " + packet.getContent();
        }
    }

    private static void renderConsole() {
        clearConsole();

        for (String entry : HISTORY) {
            System.out.println(entry);
        }
        
        System.out.println();
        renderPrompt();
        System.out.flush();
    }

    private static void renderPrompt() {
        switch (currentMode) {
            case MAIN_MENU:
                System.out.println("<<< Chat Menu >>>");
                System.out.println(" 1. Broadcast message to everyone");
                System.out.println(" 2. Show active users");
                System.out.println(" 3. Private message to a user");
                System.out.println(" 4. Quit");
                System.out.print("Choose an option: ");
                break;
            case BROADCAST:
                System.out.println("Broadcast mode. Type messages to send to everyone.");
                System.out.println("Type '/back' to return to the menu.");
                break;
            case PRIVATE_TARGET:
                System.out.println("Enter the username of the recipient.");
                System.out.println("Type '/back' to return to the menu.");
                break;
            case PRIVATE_CHAT:
                System.out.println("Private chat with " + privateTarget + ". Type messages to send directly.");
                System.out.println("Type '/back' to return to the menu.");
                break;
            default:
                break;
        }
    }

    private static void clearConsole() {
        System.out.print("\u001B[H\u001B[2J");
        System.out.flush();
    }
}
