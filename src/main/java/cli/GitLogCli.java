package cli;

import git.GitCommit;
import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static utility.Utility.*;

@Command(name = "log", mixinStandardHelpOptions = true, description = "Display history from a given commit.")
public class GitLogCli implements Callable<Integer> {
    // TODO: Optional non value = head
    @CommandLine.Parameters(index = "0",
                            description = "The starting commit.")
    String startHash;

    @Override
    public Integer call() {
        printLog("Showing history from: " + startHash, MsgLevel.INFO);
        var repo = GitRepository.findGitRepo();
        showCommit(repo, startHash);
        return 0;
    }

    void showCommit(GitRepository repo, String hash) {
        var commit = readGitObject(repo, hash);
        if (commit == null) return;
        if (!commit.format.equals("commit")) {
            printLog("Not a commit object: " + commit.format, MsgLevel.ERROR);
            return;
        }

        var commitObj = (GitCommit) commit;
        commitObj.printCommit(hash);
        var parents = commitObj.map.get("parent");
        if (parents == null) return;  // base case

        parents = parents + " ";
        int nextSpace = parents.indexOf(' ');
        while (nextSpace != -1) {
            var parentHash = parents.substring(nextSpace - 40, nextSpace);
            showCommit(repo, parentHash);
            nextSpace = parents.indexOf(' ', nextSpace + 1);
        }
    }
}
