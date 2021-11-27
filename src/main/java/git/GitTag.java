package git;

public class GitTag extends GitCommit {
    public GitTag(GitRepository repo, String data) {
        super(repo, data);
        this.format = "tag";
    }
}
