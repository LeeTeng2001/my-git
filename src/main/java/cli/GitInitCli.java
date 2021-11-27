package cli;

import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static utility.Utility.*;

@Command(name = "init", mixinStandardHelpOptions = true, description = "Initialise a git repository")
public class GitInitCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", defaultValue = ".",
                            description = "path to initialise git repository, must be empty or not exist, default is '.'")
    String path;

    @Override
    public Integer call() {
        PrintLog("Initialising git repository at: " + path, MsgLevel.INFO);
        return GitRepository.createGitRepo(path);
    }
}
