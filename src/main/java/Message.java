public class Message {
    public final int attribute;
    public final String[] parameters;

    public Message(int attribute, String[] parameters) {
        this.attribute = attribute;
        this.parameters = parameters;
    }
}