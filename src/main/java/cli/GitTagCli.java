package cli;

import git.GitRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static helper.Function.fuzzyNameMatch;
import static helper.Function.getAllRefs;
import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

@Command(name = "tag", mixinStandardHelpOptions = true,
        description = "Show tags or create a new tag for an object, usually pointing to commit")
public final class GitTagCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", defaultValue = "",  description = "The name for the tag.")
    String tagName;

    @CommandLine.Parameters(index = "1", defaultValue = "HEAD",  description = "The starting commit.")
    String hash;

    @Override
    public Integer call() {
        // Tag has lightweight tag and tag object, we do not support creation of tag object, although we could
        var repo = GitRepository.findGitRepo();
        if (repo == null) return 1;

        if (tagName.isEmpty()) {  // list tag if it's empty
            var allReferences = getAllRefs(repo);
            for (var key : allReferences.descendingKeySet()) {
                if (!key.contains("tags")) continue;  // skip non-tag reference
                System.out.println(Path.of(key).getFileName());
            }
        }
        else {  // Create tag
            var absHash = fuzzyNameMatch(repo, hash);
            if (absHash == null) {
                printLog("Name doesn't have a match: " + hash, MsgLevel.ERROR);
                return 1;
            }
            printLog("Tagging: " + tagName + " to " + absHash, MsgLevel.INFO);
            var tagFile = repo.getRepoFilePath(Path.of("refs", "tags", tagName), false);

            try {
                var writer = new PrintWriter(tagFile.toString(), StandardCharsets.UTF_8);
                writer.println(absHash);
                writer.close();
            } catch (IOException e) {
                printLog("Error when trying to write to tag file: " + tagFile, MsgLevel.ERROR);
                e.printStackTrace();
                return 1;
            }

            printLog("Successfully tagged: " + tagName, MsgLevel.SUCCESS);
        }
        return 0;
    }
}
