import cli.GitCatFileCli;
import cli.GitInitCli;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static utility.Utility.*;

@Command(name = "cli", mixinStandardHelpOptions = true, subcommands = {
        GitInitCli.class,
        GitCatFileCli.class,
})
public class Git implements Callable<Integer> {
    @Override
    public Integer call() {
        PrintLog("Cannot call git without sub-command!", MsgLevel.ERROR);
        return 1;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Git()).execute(args);
        System.exit(exitCode);
    }
}