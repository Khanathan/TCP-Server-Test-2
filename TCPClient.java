import java.util.*;
import java.io.*;
import java.net.Socket;

public class TCPClient {
    private static final String HOST = "44.252.10.0";
    private static final int PORT = 42042;
    private static final String AUTH = "whatthehelly";

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket(HOST, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner consoleReader = new Scanner(System.in);) {
            System.out.println("Sending auth");
            out.println(AUTH);
            System.out.println("Auth sent");
            int userId = Integer.parseInt(in.readLine());
            System.out.println("Assigned User ID: " + userId);

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Server: " + message);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                    e.printStackTrace();
                }
            }).start();

            // Read user input and send messages
            System.out.print("> ");
            String input;
            while ((input = consoleReader.nextLine()) != null) {
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                if (input.startsWith("MSG:") && input.length() <= 103) { // 3 for MSG + 100 chars max
                    out.println(input);
                } else if (input.equals("ALM")) {
                    out.println("ALM");
                } else if (input.startsWith("REP:") && input.length() > 4) {
                    try {
                        Integer.parseInt(input.substring(4).trim());
                        out.println(input);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid repeat count");
                    }
                } else {
                    System.out.println("Invalid command. Use MSG:<message>, ALM, or REP:<number>");
                }
                System.out.print("> ");
            }
            // Disconnect
            // Exit
            System.out.println("Client closing.");
        } catch (IOException e) {
            System.out.println("Bruh");
            e.printStackTrace();
        }
    }
}