package cli;

import git.GitCommit;
import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static helper.Function.fuzzyNameMatch;
import static helper.Function.readGitObject;
import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "log", mixinStandardHelpOptions = true, description = "Display history from a given commit.")
public final class GitLogCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", defaultValue = "HEAD",
            description = "The starting commit.")
    String name;

    @Override
    public Integer call() {
        var repo = GitRepository.findGitRepo();
        if (repo == null) return 1;

        var absHash = fuzzyNameMatch(repo, name);
        if (absHash == null) {
            printLog("Name doesn't have a match or no history: " + name, MsgLevel.ERROR);
            return 1;
        }
        printLog("Showing history from: " + absHash, MsgLevel.INFO);
        showCommit(repo, absHash);
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
