package cli;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "hash-object", mixinStandardHelpOptions = true, description = "Print content of git object")
public class GitHashObjectCli implements Callable<Integer> {
    @Override
    public Integer call() {
        System.out.println("Hash Object!");
        return null;
    }
}
