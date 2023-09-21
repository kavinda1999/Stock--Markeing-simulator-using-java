import java.io.*;
import java.net.*;

public class StockMarketClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 4148;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your trader name: ");
            String traderName = userInput.readLine();
            if (traderName == null || traderName.isEmpty()) {
                System.out.println("Invalid trader name.");
                socket.close();
                return;
            }

            System.out.println("Connected to Stock Market Server.");
            System.out.println("Available commands: buy, sell, stocks, exit");

            out.println(traderName); // Send the trader's name to the server

            String line;
            while ((line = userInput.readLine()) != null) {
                out.println(line); // Send user input to the server
                String response = in.readLine(); // Receive and print server responses
                System.out.println(response);

                if (line.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
