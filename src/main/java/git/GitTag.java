package git;

public final class GitTag extends GitCommit {
    public GitTag(GitRepository repo, byte[] data) {
        super(repo, data);
        this.format = "tag";
    }
}
