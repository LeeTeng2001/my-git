package cli;

import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static git.GitTree.getNewUncommittedLeaves;
import static helper.Utility.*;

@Command(name = "status", mixinStandardHelpOptions = true, description = "Show changes starting from directory, ONLY show changed files, does not detect changes if content is the same")
public final class GitStatusCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", defaultValue = ".", description = "Path to start from")
    String dirPath;

    @Override
    public Integer call() {
        // Very simple reimplementation of status, only check new file
        var repo = GitRepository.findGitRepo();
        if (repo == null) return 1;

        var leaves = getNewUncommittedLeaves(repo, Path.of(dirPath));
        if (leaves == null) {
            printLog("Working directory is clean", MsgLevel.INFO);
            return 0;
        }

        printLog("Modified files: ", MsgLevel.INFO);
        for (var leaf: leaves) {
            System.out.println(RED_BOLD + "->\tmodified: " + leaf.path + RESET);
        }
        return 0;
    }
}
