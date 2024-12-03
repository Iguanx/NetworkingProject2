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
    private static final List<Socket> PublicClients = new CopyOnWriteArrayList<>();
    private static final List<String> userIDsPublic = new CopyOnWriteArrayList<>();
    private static final List<String> userIDsServer = new CopyOnWriteArrayList<>();
    private static final List<Message> messageList = new CopyOnWriteArrayList<>();
    private static final List<Group> groups = new CopyOnWriteArrayList<>();


    static {
        groups.add(new Group("Group1"));
        groups.add(new Group("Group2"));
        groups.add(new Group("Group3"));
        groups.add(new Group("Group4"));
        groups.add(new Group("Group5"));
    }

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
                if (message.startsWith("%connect")){
                    //Split message into %join command and desired username
                    String[] myArray = message.split("\\s+");
                    String username;
                    //Check username exists
                    if(myArray[1] != null){
                        username = myArray[1];
                        //Check if username is already present in userIDsPublic
                        boolean contains = userIDsServer.contains(username);
                        if(contains){
                            System.out.println("User already exists: " + username);
                            output.println("User already exists: " + username);
                        } else{
                            System.out.println("User connected: " + username);
                            output.println("Message received: " + message);
                            userIDsServer.add(username);
                        }
                    }
                    displayuserIDsPublic();
                }
                if (message.startsWith("%join")){
                    //Split message into %join command and desired username
                    String[] myArray = message.split("\\s+");
                    String username;
                    //Check username exists
                    if(myArray[1] != null){
                        username = myArray[1];
                        //Check if username is already present in userIDsPublic
                        boolean contains = userIDsPublic.contains(username);
                        if(contains){
                            System.out.println("User already in group: " + username);
                            output.println("User already in group: " + username);
                        } else{
                            System.out.println("User connected: " + username);
                            output.println("Message received: " + message);
                            if(messageList.size()>1){
                                int lastIndex = messageList.size()-1;
                                output.println("\n~~~~~~~~~~~~~~~~~~~~~~\nMessage ID: " + messageList.get(lastIndex-1).getMessageID() + "\nSender: " + messageList.get(lastIndex-1).getSender() + "\nDate: " + messageList.get(lastIndex-1).getDate() + "\nSubject: " + messageList.get(lastIndex-1).getSubject() + "\nContent: " + messageList.get(lastIndex-1).getContent() + "\n~~~~~~~~~~~~~~~~~~~~~~\n~~~~~~~~~~~~~~~~~~~~~~\nMessage ID: " + messageList.get(lastIndex).getMessageID() + "\nSender: " + messageList.get(lastIndex).getSender() + "\nDate: " + messageList.get(lastIndex).getDate() + "\nSubject: " + messageList.get(lastIndex).getSubject() + "\nContent: " + messageList.get(lastIndex).getContent() + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                            } else if (messageList.size() == 1) {
                                output.println("\n~~~~~~~~~~~~~~~~~~~~~~\nMessage ID: " + messageList.get(0).getMessageID() + "\nSender: " + messageList.get(0).getSender() + "\nDate: " + messageList.get(0).getDate() + "\nSubject: " + messageList.get(0).getSubject() + "\nContent: " + messageList.get(0).getContent() + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                            } 
                            userIDsPublic.add(username);
                            PublicClients.add(clientSocket);
                            String IDlist = getUserList(userIDsPublic);
                            for (Socket client : PublicClients) {
                                PrintWriter output1 = new PrintWriter(client.getOutputStream(), true);
                                output1.println("New user joined!\n~~~~~~~~~~~~~~~~~~~~~~\nCurrent users in group: " + IDlist + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                            }
                        }
                    }
                    displayuserIDsPublic();
                }
                if (message.startsWith("%leave")){
                    //Split message into %leave command and desired username
                    String[] myArray = message.split("\\s+");
                    String username;
                    //Check username exists
                    if(myArray[1] != null){
                        username = myArray[1];
                        //Check if username is present in userIDsPublic
                        boolean contains = userIDsPublic.contains(username);
                        if(contains){
                            userIDsPublic.remove(username);
                            PublicClients.remove(clientSocket);
                            System.out.println("User removed: " + username);
                            output.println("\n~~~~~~~~~~~~~~~~~~~~~~\n" + username + " succesfully left the group\n~~~~~~~~~~~~~~~~~~~~~~\n");
                            String IDlist = getUserList(userIDsPublic);
                            for (Socket client : PublicClients) {
                                PrintWriter output1 = new PrintWriter(client.getOutputStream(), true);
                                output1.println("\n~~~~~~~~~~~~~~~~~~~~~~\n" + username + " left! Current users in group: " + IDlist + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                            }
                        } else{
                            System.out.println("Could not find user " + username);
                            output.println("Could not find user " + message);
                        }
                    }
                    displayuserIDsPublic();
                }
                if (message.startsWith("%users")){
                    //If no active users, respond accordingly
                    if(userIDsPublic.isEmpty()){
                        System.out.println("No users currently in group");
                    } else{
                        String[] inArray = message.split("\\s+");
                        String username;
                        //Check username exists
                        if(inArray[1] != null){
                            username = inArray[1];
                            //Check if username is already present in userIDsPublic
                            boolean contains = userIDsPublic.contains(username);
                            if(contains){
                                String IDlist = getUserList(userIDsPublic);
                                System.out.println("Returning list of usernames");
                                output.println("\n~~~~~~~~~~~~~~~~~~~~~~\nUsers in group: " + IDlist + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                                displayuserIDsPublic();
                            } else{
                                System.out.println("Will not return user list because this user is not in group:" + username);
                                output.println("Will not return user list because this user is not in group:" + username);
                            }
                        }
                    }
                }
                if (message.startsWith("%post")){
                    String[] messageArray = message.split("\\|\\|");
                    int messageID = messageList.size()+1;
                    Message msg = new Message(Integer.toString(messageID), messageArray[1], messageArray[2], messageArray[3], messageArray[4]);
                    messageList.add(msg);
                    for (Socket client : PublicClients) {
                        PrintWriter output1 = new PrintWriter(client.getOutputStream(), true);
                        output1.println("New message!\n~~~~~~~~~~~~~~~~~~~~~~\nMessage ID: " + Integer.toString(messageID) + "\nSender: " + messageArray[1] + "\nDate: " + messageArray[2] + "\nSubject: " + messageArray[3] + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                    }
                }
                if (message.startsWith("%message")) {
                    String[] newArray = message.split("\\s+");
                    int index = Integer.parseInt(newArray[1]) - 1;
                    if(messageList.size() <= index){
                        output.println("\n~~~~~~~~~~~~~~~~~~~~~~\nNo message with that message ID found.\n~~~~~~~~~~~~~~~~~~~~~~\n");
                    }
                    else{
                        output.println("\n~~~~~~~~~~~~~~~~~~~~~~\nMessage ID: " + messageList.get(index).getMessageID() + "\nSender: " + messageList.get(index).getSender() + "\nDate: " + messageList.get(index).getDate() + "\nSubject: " + messageList.get(index).getSubject() + "\nContent: " + messageList.get(index).getContent() + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                    }
                }

                if (message.startsWith("%groups")) {
                    StringBuilder response = new StringBuilder("Available groups:");
                    for (int i = 0; i < groups.size(); i++) {
                        response.append("\n").append(i + 1).append(". ").append(groups.get(i).getGroupName());
                    }
                    output.println(response.toString());
                }
    
                // Command to join a specific group by name or ID
                if (message.startsWith("%groupjoin")) {
                    // Split the message into the command and group identifier
                    String[] splitMessage = message.split("\\s+");
                    if (splitMessage.length < 2) {
                        output.println("Usage: %groupjoin <groupID/groupName>");
                        continue;
                    }
                
                    String groupIdentifier = splitMessage[1];
                    Group targetGroup = findGroupByIdentifier(groupIdentifier);
                
                    if (targetGroup == null) {
                        // Group not found
                        output.println("Group not found: " + groupIdentifier);
                    } else {
                        synchronized (targetGroup) {
                            String username = userIDsServer.get(connectedClients.indexOf(clientSocket));
                
                            // Check if the user is already in the group
                            if (targetGroup.getUserIDs().contains(username)) {
                                output.println("\n~~~~~~~~~~~~~~~~~~~~~~\nYou are already a member of group: " 
                                        + targetGroup.getGroupName() + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                            } else {
                                // Add the user to the group's user list and socket list
                                targetGroup.getUserIDs().add(username);
                                targetGroup.getGroupClients().add(clientSocket);
                
                                output.println("\n~~~~~~~~~~~~~~~~~~~~~~\nSuccessfully joined group: " 
                                        + targetGroup.getGroupName() + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                
                                // Notify other members of the group
                                for (Socket client : targetGroup.getGroupClients()) {
                                    if (client != clientSocket) { // Avoid notifying the joining client
                                        try {
                                            PrintWriter groupOutput = new PrintWriter(client.getOutputStream(), true);
                                            groupOutput.println("\n~~~~~~~~~~~~~~~~~~~~~~\nUser " + username 
                                                    + " has joined group: " + targetGroup.getGroupName() + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                                        } catch (IOException e) {
                                            System.err.println("Error notifying client: " + e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (message.startsWith("%groupusers")) {
                    String[] inArray = message.split("\\s+");
                    if (inArray.length < 2) {
                        output.println("Usage: %groupusers <groupID/groupName>");
                        continue;
                    }
                
                    String groupIdentifier = inArray[1];
                    Group targetGroup = findGroupByIdentifier(groupIdentifier);
                
                    if (targetGroup == null) {
                        output.println("Group not found: " + groupIdentifier);
                    } else {
                        String username = userIDsServer.get(connectedClients.indexOf(clientSocket));
                        if (!isUserInGroup(username, targetGroup)) {
                            output.println("You are not a member of group: " + targetGroup.getGroupName());
                        } else {
                            List<String> userIDs = targetGroup.getUserIDs();
                            if (userIDs.isEmpty()) {
                                output.println("No users currently in group: " + groupIdentifier);
                            } else {
                                String IDlist = String.join(", ", userIDs);
                                output.println("\n~~~~~~~~~~~~~~~~~~~~~~\nUsers in group " + groupIdentifier + ": " + IDlist + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                            }
                        }
                    }
                }
                
                
                if (message.startsWith("%grouppost")) {
                    String[] messageArray = message.split("\\|\\|");
                    if (messageArray.length < 5) {
                        output.println("Usage: %grouppost <groupID/groupName>||<username>||<timestamp>||<subject>||<content>");
                        continue;
                    }
                
                    String groupIdentifier = messageArray[0].split("\\s+")[1];
                    String sender = messageArray[1];
                    String timestamp = messageArray[2];
                    String subject = messageArray[3];
                    String content = messageArray[4];
                
                    Group targetGroup = findGroupByIdentifier(groupIdentifier);
                    if (targetGroup == null) {
                        output.println("Group not found: " + groupIdentifier);
                    } else if (!isUserInGroup(sender, targetGroup)) {
                        output.println("You are not a member of group: " + targetGroup.getGroupName());
                    } else {
                        synchronized (targetGroup) {
                            int messageID = targetGroup.getGroupMessages().size() + 1;
                            Message newMessage = new Message(Integer.toString(messageID), sender, timestamp, subject, content);
                            targetGroup.getGroupMessages().add(newMessage);
                
                            for (Socket client : targetGroup.getGroupClients()) {
                                try {
                                    PrintWriter groupOutput = new PrintWriter(client.getOutputStream(), true);
                                    groupOutput.println("New message in group " + targetGroup.getGroupName() + ":\n"
                                            + "~~~~~~~~~~~~~~~~~~~~~~\nMessage ID: " + messageID + "\nSender: " + sender
                                            + "\nDate: " + timestamp + "\nSubject: " + subject
                                            + "\nContent: " + content + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                                } catch (IOException e) {
                                    System.err.println("Error notifying client: " + e.getMessage());
                                }
                            }
                        }
                        output.println("Message successfully posted to group " + targetGroup.getGroupName());
                    }
                }
                
                
                if (message.startsWith("%groupleave")) {
                    String[] splitMessage = message.split("\\s+", 2);
                    if (splitMessage.length < 2) {
                        output.println("Usage: %groupleave <groupID/groupName>");
                        continue;
                    }
                
                    String groupIdentifier = splitMessage[1];
                    Group targetGroup = findGroupByIdentifier(groupIdentifier);
                
                    if (targetGroup == null) {
                        output.println("Group not found: " + groupIdentifier);
                    } else {
                        String username = userIDsServer.get(connectedClients.indexOf(clientSocket));
                        if (!isUserInGroup(username, targetGroup)) {
                            output.println("You are not a member of group: " + targetGroup.getGroupName());
                        } else {
                            synchronized (targetGroup) {
                                targetGroup.getUserIDs().remove(username);
                                targetGroup.getGroupClients().remove(clientSocket);
                
                                output.println("\n~~~~~~~~~~~~~~~~~~~~~~\nYou have successfully left group: "
                                        + targetGroup.getGroupName() + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                
                                for (Socket client : targetGroup.getGroupClients()) {
                                    try {
                                        PrintWriter groupOutput = new PrintWriter(client.getOutputStream(), true);
                                        groupOutput.println("\n~~~~~~~~~~~~~~~~~~~~~~\nUser " + username
                                                + " has left group: " + targetGroup.getGroupName() + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                                    } catch (IOException e) {
                                        System.err.println("Error notifying client: " + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
                
                
                if (message.startsWith("%groupmessage")) {
                    String[] splitMessage = message.split("\\s+", 3);
                    if (splitMessage.length < 3) {
                        output.println("Usage: %groupmessage <groupID/groupName> <messageID>");
                        continue;
                    }
                
                    String groupIdentifier = splitMessage[1];
                    int messageID;
                    try {
                        messageID = Integer.parseInt(splitMessage[2]);
                    } catch (NumberFormatException e) {
                        output.println("Invalid message ID. Please enter a numeric value.");
                        continue;
                    }
                
                    Group targetGroup = findGroupByIdentifier(groupIdentifier);
                    if (targetGroup == null) {
                        output.println("Group not found: " + groupIdentifier);
                    } else if (!isUserInGroup(userIDsServer.get(connectedClients.indexOf(clientSocket)), targetGroup)) {
                        output.println("You are not a member of group: " + targetGroup.getGroupName());
                    } else {
                        synchronized (targetGroup) {
                            if (messageID > 0 && messageID <= targetGroup.getGroupMessages().size()) {
                                Message msg = targetGroup.getGroupMessages().get(messageID - 1);
                                output.println("\n~~~~~~~~~~~~~~~~~~~~~~\nMessage ID: " + msg.getMessageID()
                                        + "\nSender: " + msg.getSender()
                                        + "\nDate: " + msg.getDate()
                                        + "\nSubject: " + msg.getSubject()
                                        + "\nContent: " + msg.getContent()
                                        + "\n~~~~~~~~~~~~~~~~~~~~~~\n");
                            } else {
                                output.println("No message with ID " + messageID + " found in group " + groupIdentifier);
                            }
                        }
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
    private static void displayuserIDsPublic() {
        System.out.println("Usernames in group:");
        if (userIDsPublic.isEmpty()) {
            System.out.println("No usernames in group.");
        } else {
            for (String user : userIDsPublic) {
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

    private static Group findGroupByIdentifier(String identifier) {
        try {
            int groupIndex = Integer.parseInt(identifier) - 1;
            if (groupIndex >= 0 && groupIndex < groups.size()) {
                return groups.get(groupIndex);
            }
        } catch (NumberFormatException e) {
            for (Group group : groups) {
                if (group.getGroupName().equalsIgnoreCase(identifier)) {
                    return group;
                }
            }
        }
        return null;
    }

    private static boolean isUserInGroup(String username, Group group) {
        return group.getUserIDs().contains(username);
    }
    
    
}