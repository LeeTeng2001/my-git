package cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "cat-file", mixinStandardHelpOptions = true, description = "Print content of git object")
public class GitCatFileCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "hash value of the git object")
    String hashVal;

    @Override
    public Integer call() {
        System.out.println("Cat File! " + hashVal);
        return null;
    }
}
