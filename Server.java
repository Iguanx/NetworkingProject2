/*
Network Project 2
Changing stuff here too
Adding another line here as well
*/
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    // Thread-safe list to store client information
    private static final List<Socket> connectedClients = new CopyOnWriteArrayList<>();
    private static final List<String> userIDs = new CopyOnWriteArrayList<>();
    public static void main(String[] args) {
        int port = 6789; // Port to listen on

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Web Server started and listening on port: " + port);

            while (true) {
                // Accept a new client connection
                Socket clientSocket = serverSocket.accept();
                connectedClients.add(clientSocket);
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Handle client communication in a separate thread
                new Thread(() -> handleClient(clientSocket)).start();

                //Display conected clients
                displayConnectedClients();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String message;
            while ((message = in.readLine()) != null) {
                // Print the received message to the terminal
                System.out.println("Received: " + message);
                if (message.startsWith("%join")){
                    //Split message into %join command and desired username
                    String[] myArray = message.split("\\s+");
                    String username;
                    //Check username exists
                    if(myArray[1] != null){
                        username = myArray[1];
                        //Check if username is already present in userIDs
                        boolean contains = userIDs.contains(username);
                        if(contains){
                            System.out.println("User already exists: " + username);
                            output.println("User already exists: " + username);
                        } else{
                            System.out.println("User connected: " + username);
                            output.println("Message received: " + message);
                            userIDs.add(username);
                        }
                    }
                    displayuserIDs();
                }
                if (message.startsWith("%leave")){
                    //Split message into %leave command and desired username
                    String[] myArray = message.split("\\s+");
                    String username;
                    //Check username exists
                    if(myArray[1] != null){
                        username = myArray[1];
                        //Check if username is present in userIDs
                        boolean contains = userIDs.contains(username);
                        if(contains){
                            userIDs.remove(username);
                            System.out.println("User removed: " + username);
                            output.println("Message received: " + message);
                        } else{
                            System.out.println("Could not find user " + username);
                            output.println("Could not find user " + message);
                        }
                    }
                    displayuserIDs();
                }
                if (message.startsWith("%users")){
                    //If no active users, respond accordingly
                    if(userIDs.isEmpty()){
                        System.out.println("No users currently in group");
                    } else{
                        String IDlist = getUserList(userIDs);
                        System.out.println("Returning list of usernames");
                        output.println("Users: " + IDlist);
                        displayuserIDs();
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                connectedClients.remove(clientSocket);
                System.out.println("Client disconnected.");
                displayConnectedClients();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private static void displayConnectedClients() {
        System.out.println("Connected clients:");
        if (connectedClients.isEmpty()) {
            System.out.println("No clients connected.");
        } else {
            for (Socket client : connectedClients) {
                System.out.println(client.getInetAddress() + ":" + client.getPort());
            }
        }
    }
    private static void displayuserIDs() {
        System.out.println("Usernames in group:");
        if (userIDs.isEmpty()) {
            System.out.println("No usernames in group.");
        } else {
            for (String user : userIDs) {
                System.out.println(user);
            }
        }
    }
    public static String getUserList(List<String> users) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < users.size(); i++) {
            result.append(users.get(i)); 
            if (i < users.size() - 1) {
                result.append(", "); // Append a comma if it's not the last item
            }
        }

        return result.toString();
    }
}