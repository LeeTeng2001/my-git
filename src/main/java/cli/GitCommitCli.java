package cli;

import git.GitCommit;
import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static git.GitTree.commitFromPath;
import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "commit", mixinStandardHelpOptions = true, description = "Save current state")
public class GitCommitCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0..", defaultValue = "No commit message", description = "Commit message")
    List<String> messages;

    @CommandLine.Option(names = {"-y", "yes"}, description = "turn this option on to truly commit")
    boolean commit;

    @Override
    public Integer call() {
        var repo = GitRepository.findGitRepo();
        if (repo == null) return 1;

        var message = String.join(" ", messages);
        var topLeaf = commitFromPath(repo, repo.getRelativeWorkingDir(), commit);
        if (topLeaf == null) {
            printLog("Nothing to commit to", MsgLevel.INFO);
            return 0;
        }

        // Create new commit file and point update head
        var commitHash = GitCommit.createCommit(repo, topLeaf.sha, message, commit);
        if (commitHash == null) return 0;

        if (!commit) printLog("Simulated commit hash: " + commitHash, MsgLevel.INFO);
        else printLog("Commit hash: " + commitHash, MsgLevel.SUCCESS);

        return 0;
    }
}
