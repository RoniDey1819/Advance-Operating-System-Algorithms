package PackageRaymond;

public class Message {
    public enum Type { REQUEST, TOKEN }

    private final Type type;
    private final int senderId;
    private final int requesterId;

    public Message(Type type, int senderId, int requesterId) {
        this.type = type;
        this.senderId = senderId;
        this.requesterId = requesterId;
    }

    public Type getType() { return type; }
    public int getSenderId() { return senderId; }
    public int getRequesterId() { return requesterId; }

    @Override
    public String toString() {
        return type + " from " + senderId + " for " + requesterId;
    }
}
