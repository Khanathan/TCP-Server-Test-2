import java.util.*;
import java.io.*;

public class TCPClient {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);

        int port = 0;
        System.out.print("Please enter server port number: ");
        String portStr = in.nextLine();
        port = Integer.parseInt(portStr);

        // Connect to server

        while (true) {
            System.out.print("> ");
            String input = in.nextLine().strip();
            if (input == ":q") {
                break;
            }
            System.out.println("You just said: " + input);
        }

        // Disconnect

        // Exit
        System.out.println("Client closing.");
    }
}