package cli;

import git.GitCommit;
import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.concurrent.Callable;

import static git.GitTree.commitFromPath;
import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "commit", mixinStandardHelpOptions = true, description = "Save current state")
public class GitCommitCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0..", description = "Commit message")
    List<String> messages;

    @CommandLine.Option(names = {"-y", "yes"}, description = "turn this option on to truly commit")
    boolean commit;

    @Override
    public Integer call() {
        var message = String.join(" ", messages);
        var repo = GitRepository.findGitRepo();
        var topLeaf = commitFromPath(repo, repo.getRelativeWorkingDir(), commit);

        // Create new commit file and point update head
        var commitHash = GitCommit.createCommit(repo, topLeaf.sha, message, commit);

        if (!commit) printLog("Dry run commit", MsgLevel.SUCCESS);
        else printLog("New Commit: " + commitHash, MsgLevel.SUCCESS);

        return 0;
    }
}
