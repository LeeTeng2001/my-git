package cli;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "status", mixinStandardHelpOptions = true, description = "Show new file")
public class GitStatusCli implements Callable<Integer> {
    @Override
    public Integer call() {
        // Very simple reimplementation of status, only check new file
        printLog("New files: ", MsgLevel.INFO);
        return 0;
    }
}
