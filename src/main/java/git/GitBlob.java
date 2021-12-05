package git;

public final class GitBlob extends GitObject {
    byte[] data;

    public GitBlob(GitRepository repo, byte[] data) {
        this.format = "blob";
        this.repo = repo;
        deserialize(data);
    }

    @Override
    public byte[] serialize() {
        return data;
    }

    @Override
    public String serializeString() {
        return new String(data);
    }

    @Override
    public void deserialize(byte[] data) {
        this.data = data;
    }
}
