import cli.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static utility.Utility.MsgLevel;
import static utility.Utility.printLog;

// TODO: Add commit, real checkout, check status, I think no staging

@Command(name = "cli", mixinStandardHelpOptions = true, subcommands = {
        GitInitCli.class,
        GitCatFileCli.class,
        GitHashObjectCli.class,
        GitLogCli.class,
        GitLsTreeCli.class,
        GitReconstructCli.class,
})
public class Git implements Callable<Integer> {
    @Override
    public Integer call() {
        printLog("Cannot call git without sub-command!", MsgLevel.ERROR);
        return 1;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Git()).execute(args);
        System.exit(exitCode);
    }
}