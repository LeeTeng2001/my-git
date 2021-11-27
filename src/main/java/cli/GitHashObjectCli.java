package cli;

import git.GitBlob;
import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static utility.Utility.*;

@Command(name = "hash-object", mixinStandardHelpOptions = true, description = "Compute object ID and optionally create blob file")
public class GitHashObjectCli implements Callable<Integer> {
    enum ObjType {blob, tree, commit, tag}

    @CommandLine.Option(names = {"-t", "type"}, defaultValue = "blob", description = "format of git object: [${COMPLETION-CANDIDATES}], default is blob")
    ObjType format;

    @CommandLine.Option(names = {"-w", "write"}, defaultValue = "false", description = "Actually write the object into the database?")
    Boolean write;

    @CommandLine.Parameters(index = "0", description = "Path of the original object")
    String objPath;

    @Override
    public Integer call() {
        // TODO: Consider format
        try {
            var content = Files.readString(Path.of(objPath));
            var curRepo = GitRepository.findGitRepo();
            var gitObject = new GitBlob(curRepo, content);
            var computedHash = writeGitObject(gitObject, write);
            printLog("Object hash is: " + computedHash, MsgLevel.INFO);
        } catch (IOException e) {
            printLog("File doesn't exist at: " + objPath, MsgLevel.ERROR);
            e.printStackTrace();
            return 1;
        }

        return 0;
    }
}
