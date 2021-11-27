package git;

public abstract class GitObject {
    public GitRepository repo = null;
    public String format = null;

    abstract public String serialize();
    abstract public void deserialize(String data);
}
