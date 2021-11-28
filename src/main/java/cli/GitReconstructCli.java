package cli;

import git.GitCommit;
import git.GitRepository;
import git.GitTree;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static utility.Utility.*;

@Command(name = "reconstruct", mixinStandardHelpOptions = true, description = "Reconstruct a commit inside of a directory.")
public class GitReconstructCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "Commit or tree to checkout")
    String name;

    @CommandLine.Parameters(index = "1", description = "Directory must be EMPTY, instantiate the tree in that directory")
    String directory;

    @Override
    public Integer call() {
        var repo = GitRepository.findGitRepo();
        var absHash = fuzzyNameMatch(repo, name);
        if (absHash == null) {
            printLog("Name doesn't have a match: " + name, MsgLevel.ERROR);
            return 1;
        }

        var gitObj = readGitObject(repo, absHash);
        if (gitObj.format.equals("commit")) {  // turn commit into tree
            var commit = ((GitCommit) gitObj).map.get("tree");
            gitObj = readGitObject(repo, commit);
        }
        else if (!gitObj.format.equals("tree")) {
            printLog("git object is not a tree or commit: " + gitObj.format, MsgLevel.ERROR);
            return 1;
        }

        // directory must be empty or not exist
        var file = Path.of(directory).toFile();
        if (file.exists()) {
            if (file.isFile()) {
                printLog("Path is a file: " + directory, MsgLevel.ERROR);
                return 1;
            }
            else if (file.list().length != 0) {
                printLog("Directory is not empty: " + directory, MsgLevel.ERROR);
                return 1;
            }
        }
        else file.mkdirs();

        // instantiate the tree content
        reconstructTree(repo, (GitTree) gitObj, file.toPath());

        printLog("Reconstructed commit/tree at: " + directory, MsgLevel.SUCCESS);
        return 0;
    }
}