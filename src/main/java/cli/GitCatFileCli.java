package cli;

import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static helper.Function.fuzzyNameMatch;
import static helper.Function.readGitObject;
import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;


@Command(name = "cat-file", mixinStandardHelpOptions = true, description = "Print content of git object")
public class GitCatFileCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "hash value of the git object")
    String name;

    @Override
    public Integer call() {
        var repo = GitRepository.findGitRepo();
        var absHash = fuzzyNameMatch(repo, name);
        if (absHash == null) {
            printLog("Name doesn't have a match: " + name, MsgLevel.ERROR);
            return 1;
        }

        var obj = readGitObject(repo, absHash);
        printLog("Content of obj " + absHash + " is: ", MsgLevel.INFO);
        System.out.println(obj.serializeString());
        return 0;
    }
}
