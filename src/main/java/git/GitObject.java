package git;

public abstract class GitObject {
    GitRepository repo = null;
    String format = null;

    abstract public String serialize();
    abstract public void deserialize(String data);
}
