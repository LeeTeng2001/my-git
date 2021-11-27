package git;

public class GitBlob extends GitObject {
    public GitBlob(GitRepository repo, String data) {
        this.repo = repo;
        deserialize(data);
    }

    @Override
    public String serialize() {

    }

    @Override
    public void deserialize(String data) {

    }
}
