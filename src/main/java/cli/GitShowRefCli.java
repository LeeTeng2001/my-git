package cli;

import git.GitRepository;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static helper.Function.getAllRefs;
import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "show-ref", mixinStandardHelpOptions = true, description = "List all references")
public class GitShowRefCli implements Callable<Integer> {
    @Override
    public Integer call() {
        var repo = GitRepository.findGitRepo();
        var allReferences = getAllRefs(repo);

        printLog("Showing all references: ", MsgLevel.INFO);
        for (var key : allReferences.descendingKeySet()) {
            System.out.printf("%s\t%s\n", allReferences.get(key), repo.getRelativePath(Path.of(key)));
        }

        return 0;
    }
}
