package cli;

import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static utility.Utility.*;


@Command(name = "cat-file", mixinStandardHelpOptions = true, description = "Print content of git object")
public class GitCatFileCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "hash value of the git object")
    String hashVal;

    @Override
    public Integer call() {
        var repo = GitRepository.findGitRepo();
        var obj = readGitObject(repo, hashVal);
        printLog("Content of obj: " + hashVal, MsgLevel.INFO);
        System.out.println(obj.serialize());
        return 0;
    }
}
