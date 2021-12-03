package cli;

import git.GitCommit;
import git.GitRepository;
import git.GitTree;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static helper.Function.fuzzyNameMatch;
import static helper.Function.readGitObject;
import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "ls-tree", mixinStandardHelpOptions = true, description = "Pretty-print a tree object.")
public class GitLsTreeCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "hash value of the tree object")
    String name;

    @Override
    public Integer call() {
        var repo = GitRepository.findGitRepo();
        if (repo == null) return 1;

        var absHash = fuzzyNameMatch(repo, name);
        if (absHash == null) {
            printLog("Name doesn't have a match: " + name, MsgLevel.ERROR);
            return 1;
        }

        var obj = readGitObject(repo, absHash);

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
            System.out.println(leaf.getFmtOutput());
        }

        return 0;
    }
}
