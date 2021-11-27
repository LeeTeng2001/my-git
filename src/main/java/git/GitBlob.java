package git;

public class GitBlob extends GitObject {
    String data;

    public GitBlob(GitRepository repo, byte[] data) {
        this.format = "blob";
        this.repo = repo;
        deserialize(data);
    }

    @Override
    public String serialize() {
        return data;
    }

    @Override
    public void deserialize(byte[] data) {
        this.data = new String(data);
    }
}
