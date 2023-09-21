import java.io.*;
import java.net.*;
import java.util.*;

public class StockMarketServer {
    private static final int PORT = 4148;
    private static final String[] STOCKS = {"AAPL", "GOOGL", "TSLA", "AMZN"};
    private static final int INITIAL_STOCK_PRICE = 100;
    private static final int MAX_PRICE_CHANGE = 10;

    private static Map<String, Integer> stockPrices = new HashMap<>();
    private static Map<String, Integer> traderBalances = new HashMap<>();

    public static void main(String[] args) {
        initializeStockPrices();
        initializeTraderBalances();

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Stock Market Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                Thread clientThread = new ClientHandler(clientSocket);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String traderName = in.readLine();
                if (traderName == null) return;

                traderBalances.put(traderName, 10000); // Initial balance

                out.println("Welcome to the Stock Market, " + traderName + "!");
                out.println("Available Stocks: " + String.join(", ", STOCKS));

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("exit")) {
                        break;
                    } else if (line.equalsIgnoreCase("stocks")) {
                        sendStockPrices();
                    } else if (line.startsWith("buy ")) {
                        executeBuyOrder(traderName, line.substring(4));
                    } else if (line.startsWith("sell ")) {
                        executeSellOrder(traderName, line.substring(5));
                    } else {
                        out.println("Invalid command. Available commands: buy, sell, stocks, exit");
                    }
                }

                clientSocket.close();
                System.out.println("Client disconnected: " + traderName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendStockPrices() {
            for (String stock : STOCKS) {
                out.println(stock + ": " + stockPrices.get(stock));
            }
        }

        private void executeBuyOrder(String traderName, String order) {
            String[] parts = order.split(" ");
            if (parts.length != 2) {
                out.println("Invalid buy order format. Use: buy <stock>");
                return;
            }
            String stock = parts[1];

            if (!stockPrices.containsKey(stock)) {
                out.println("Invalid stock symbol: " + stock);
                return;
            }

            int price = stockPrices.get(stock);
            int balance = traderBalances.get(traderName);

            if (balance < price) {
                out.println("Not enough balance to buy " + stock + " at $" + price);
                return;
            }

            traderBalances.put(traderName, balance - price);
            out.println("Bought " + stock + " at $" + price);
        }

        private void executeSellOrder(String traderName, String order) {
            String[] parts = order.split(" ");
            if (parts.length != 2) {
                out.println("Invalid sell order format. Use: sell <stock>");
                return;
            }
            String stock = parts[1];

            if (!stockPrices.containsKey(stock)) {
                out.println("Invalid stock symbol: " + stock);
                return;
            }

            int price = stockPrices.get(stock);
            int balance = traderBalances.get(traderName);

            traderBalances.put(traderName, balance + price);
            out.println("Sold " + stock + " at $" + price);
        }
    }

    private static void initializeStockPrices() {
        for (String stock : STOCKS) {
            stockPrices.put(stock, INITIAL_STOCK_PRICE);
        }
    }

    private static void initializeTraderBalances() {
        // Trader balances are initialized in the ClientHandler
    }
}
