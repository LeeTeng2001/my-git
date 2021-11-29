package git;

// TODO: Store object in bytes to avoid wrong format
public abstract class GitObject {
    public GitRepository repo = null;
    public String format = null;

    abstract public byte[] serialize();
    abstract public String serializeString();
    abstract public void deserialize(byte[] data);
}
