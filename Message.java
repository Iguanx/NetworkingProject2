public class Message {
    // Instance variables
    private String messageID;
    private String sender;
    private String date;
    private String subject;
    private String content;

    // Constructor
    public Message(String messageID, String sender, String date, String subject, String content) {
        this.messageID = messageID;
        this.sender = sender;
        this.date = date;
        this.subject = subject;
        this.content = content;
    }

    // Default constructor
    public Message() {}

    // Getter and Setter for MessageID
    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    // Getter and Setter for Sender
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    // Getter and Setter for Date
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Getter and Setter for Subject
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    // Getter and Setter for Content
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Optional: Override toString() for easy printing
    @Override
    public String toString() {
        return "Message{" +
                "MessageID='" + messageID + '\'' +
                ", Sender='" + sender + '\'' +
                ", Date='" + date + '\'' +
                ", Subject='" + subject + '\'' +
                ", Content='" + content + '\'' +
                '}';
    }
}