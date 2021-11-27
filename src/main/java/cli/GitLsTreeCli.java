package cli;

import git.GitCommit;
import git.GitRepository;
import git.GitTree;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static utility.Utility.*;

@Command(name = "ls-tree", mixinStandardHelpOptions = true, description = "Pretty-print a tree object.")
public class GitLsTreeCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "hash value of the tree object")
    String treeHash;

    @Override
    public Integer call() {
        var repo = GitRepository.findGitRepo();
        var obj = readGitObject(repo, treeHash);

        if (obj.format.equals("commit")) {
            var commit = (GitCommit) obj;
            obj = readGitObject(repo, commit.map.get("tree"));
        }
        else if (!obj.format.equals("tree")) {
            printLog("Object is not a tree: " + obj.format, MsgLevel.ERROR);
            return 1;
        }

        var treeObj = (GitTree) obj;
        for (var leaf: treeObj.leaves) {
            System.out.print(String.format("%6s", leaf.mode).replace(' ', '0'));
            System.out.printf(" %s %s\t%s\n", leaf.fmt, leaf.sha, leaf.path);
        }

        return 0;
    }
}