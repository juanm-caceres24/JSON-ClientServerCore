# JSON-ClientServerCore

JSON-ClientServerCore/
│
├── lib/
│   └── gson-2.11.0.jar
├── src/
│   │
│   ├── common/                     # Shared models and utilities for both Client and Server
│   │   ├── JsonPacket.java         # Base POJO representing the network message structure
│   │   └── ProtocolParser.java     # Serialization and deserialization wrapper using Google Gson
│   │
│   ├── server/                     # Core Server Engine (Generic Infrastructure)
│   │   ├── ServerCore.java         # Initializes ServerSocket and handles the client connection loop
│   │   ├── ClientHandler.java      # Dedicated thread managing network I/O for a single connected client
│   │   └── ServerListener.java     # Core event interface used to attach custom business logic
│   │
│   └── client/                     # Core Client Engine (Generic Infrastructure)
│       ├── ClientCore.java         # Establishes TCP handshake and holds core network references
│       ├── ReceiverThread.java     # Background thread that constantly listens for inbound JSON streams
│       └── SenderService.java      # Utility service designed to dispatch outbound packets efficiently
│
└── README.md                       # Technical framework documentation
