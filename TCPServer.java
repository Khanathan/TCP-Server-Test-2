import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TCPServer {
    private static final int PORT = 42042;
    private static final String VALID_AUTH = "whatthehelly"; // Example valid auth string
    private static final ConcurrentHashMap<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    private static int nextUserId = 1;

    public static void main(String[] args) throws IOException {
        InetAddress bindAddr = InetAddress.getByName("0.0.0.0");
        try (

                ServerSocket serverSocket = new ServerSocket(PORT, 50, bindAddr);) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Incoming connection attempted");
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            System.out.println("handleClient() started.");

            // Read and validate auth string
            // char[] authBuffer = new char[VALID_AUTH.length()];
            String auth = in.readLine();
            System.out.println("auth = " + auth);
            if (auth.length() != VALID_AUTH.length()
                    || !auth.equals(VALID_AUTH)) {
                System.out.println("Client rejected, auth: " + auth);
                clientSocket.close(); // Drop connection if auth invalid
                return;
            }
            System.out.println("auth: " + auth);

            // Assign user ID and send to client
            int userId = nextUserId++;
            System.out.println("Connected to a client, user ID: " + userId);
            out.println(userId);

            // Add client to list
            ClientHandler clientHandler = new ClientHandler(userId, clientSocket, in, out);
            clients.put(userId, clientHandler);

            // Handle client messages
            clientHandler.handleMessages();

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    static class ClientHandler {
        private final int userId;
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;

        public ClientHandler(int userId, Socket socket, BufferedReader in, PrintWriter out) {
            this.userId = userId;
            this.socket = socket;
            this.in = in;
            this.out = out;
        }

        public void handleMessages() throws IOException {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.length() < 3) {
                    continue; // Invalid message
                }
                String token = message.substring(0, 3);
                String data = message.length() > 3 ? message.substring(3) : "";

                switch (token) {
                    case "MSG":
                        if (data.length() <= 100) {
                            System.out.println("Received message: " + data);
                            broadcast(userId, data);
                        }
                        break;
                    case "ALM":
                        System.out.println("Alarm received from user " + userId);
                        // Handle alarm (e.g., log or notify)
                        break;
                    case "REP":
                        try {
                            int repeatCount = Integer.parseInt(data.trim());
                            System.out.println("Repping " + repeatCount + " times.");
                            for (int i = 0; i < repeatCount; i++) {
                                broadcast(0, "yo"); // 0 means server-originated
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid integer
                        }
                        break;
                    default:
                        // Ignore unknown token
                        System.out.println("wat");
                        break;
                }
            }
            clients.remove(userId);
        }

        private void broadcast(int senderId, String message) {
            for (ClientHandler client : clients.values()) {
                if (client.userId != senderId) {
                    client.out.println(senderId + ": " + message);
                }
            }
        }
    }
}