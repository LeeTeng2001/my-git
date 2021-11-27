package git;

public class GitTree extends GitObject {
    public GitTree(GitRepository repo, String data) {
        this.format = "tree";
        deserialize(data);
    }

    @Override
    public String serialize() {
        return null;
    }

    @Override
    public void deserialize(String data) {

    }
}
