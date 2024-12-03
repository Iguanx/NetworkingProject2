import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Group {
    private String groupName;
    private List<String> userIDs; // Usernames in the group
    private List<Socket> groupClients; // Client sockets
    private List<Message> groupMessages; // Messages in the group

    public Group(String groupName) {
        this.groupName = groupName;
        this.userIDs = new CopyOnWriteArrayList<>();
        this.groupClients = new CopyOnWriteArrayList<>();
        this.groupMessages = new CopyOnWriteArrayList<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getUserIDs() {
        return userIDs;
    }

    public List<Socket> getGroupClients() {
        return groupClients;
    }

    public List<Message> getGroupMessages() {
        return groupMessages;
    }
}

