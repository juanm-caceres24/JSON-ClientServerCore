# JSON-ClientServerCore

JSON-ClientServerCore/
│
├── lib/                                # External runtime dependencies
│   └── gson-2.11.0.jar                 # Google Gson: library for serializing/deserializing JSON objects
│
├── src/
│   ├── client/                         # Core Client Engine (Generic Infrastructure)
│   │   ├── ClientCore.java             # Establishes TCP handshake, connection lifecycle and core network references
│   │   ├── ClientPacketListener.java   # Interface/implementation that receives parsed packets and routes them to handlers
│   │   ├── ReceiverThread.java         # Background thread that constantly listens for inbound JSON streams
│   │   └── SenderService.java          # Utility service designed to dispatch outbound packets efficiently
│   │
│   ├── common/                         # Shared models and utilities for both Client and Server
│   │   ├── JsonPacket.java             # Base POJO representing the network message structure
│   │   └── ProtocolParser.java         # Serialization and deserialization wrapper using Google Gson
│   │
│   ├── server/                         # Core Server Engine (Generic Infrastructure)
│   │   ├── ServerCore.java             # Initializes ServerSocket and handles the client connection loop
│   │   ├── ClientHandler.java          # Dedicated thread managing network I/O for a single connected client
│   │   └── ServerListener.java         # Core event interface used to attach custom business logic
│   │
│   └── testchat/
│       ├── ChatClientApp.java          # Minimal example client application that demonstrates connecting and exchanging messages
│       ├── ChatLogic.java              # High-level chat logic (message formatting, command handling, UI glue for examples)
│       └── ChatServerApp.java          # Minimal example server application wiring ServerCore + ServerListener for a chat demo
│
└── README.md                           # Technical framework documentation
