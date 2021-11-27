package git;

public class GitBlob extends GitObject {
    String data;

    public GitBlob(GitRepository repo, String data) {
        this.repo = repo;
        this.format = "blob";
        deserialize(data);
    }

    @Override
    public String serialize() {
        return data;
    }

    @Override
    public void deserialize(String data) {
        this.data = data;
    }
}
