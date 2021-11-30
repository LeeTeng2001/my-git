package cli;

import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "init", mixinStandardHelpOptions = true, description = "Initialise a git repository")
public class GitInitCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", defaultValue = ".",
                            description = "path to initialise git repository, must be empty or not exist, default is '.'")
    String path;
    @CommandLine.Parameters(index = "1", description = "Author name")
    String name;
    @CommandLine.Parameters(index = "2", description = "Author email")
    String email;

    @Override
    public Integer call() {
        printLog("Initialising git repository at: " + path + " for " + name + ", " + email, MsgLevel.INFO);
        return GitRepository.createGitRepo(path, name, email);
    }
}
