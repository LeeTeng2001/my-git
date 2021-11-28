package cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "commit", mixinStandardHelpOptions = true, description = "Show new file")
public class GitCommitCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "Commit message")
    String message;

    @Override
    public Integer call() {
        printLog("Committing", MsgLevel.INFO);
        return 0;
    }
}
