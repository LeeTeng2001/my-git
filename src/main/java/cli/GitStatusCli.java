package cli;

import git.GitRepository;
import git.GitTree;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static git.GitTree.treePathToLeaf;
import static helper.Function.writeGitObject;
import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "status", mixinStandardHelpOptions = true, description = "Show changes")
public class GitStatusCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", defaultValue = ".", description = "Path to start from")
    String dirPath;

    @Override
    public Integer call() {
        // Very simple reimplementation of status, only check new file
        var repo = GitRepository.findGitRepo();
        var leaf = treePathToLeaf(repo, Path.of(dirPath));
        printLog("Tree hash: " + leaf.sha, MsgLevel.SUCCESS);
        return 0;
    }
}
