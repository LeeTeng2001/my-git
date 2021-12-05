package git;

public abstract class GitObject {
    public GitRepository repo = null;
    public String format = null;

    abstract public byte[] serialize();
    abstract public String serializeString();
    abstract public void deserialize(byte[] data);
}
