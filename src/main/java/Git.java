import cli.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "git", mixinStandardHelpOptions = true, subcommands = {
        GitInitCli.class,
        GitCatFileCli.class,
        GitHashObjectCli.class,
        GitLogCli.class,
        GitLsTreeCli.class,
        GitRestoreCli.class,
        GitShowRefCli.class,
        GitTagCli.class,
        GitStatusCli.class,
        GitCommitCli.class,
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