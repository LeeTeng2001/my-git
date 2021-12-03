package git;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;

import static helper.Function.*;
import static helper.Utility.*;

public class GitCommit extends GitObject {
    public TreeMap<String, String> map;

    public GitCommit(GitRepository repo, byte[] data) {
        this.format = "commit";
        this.repo = repo;
        map = new TreeMap<>();
        deserialize(data);
    }

    public GitCommit(GitRepository repo) {
        this.format = "commit";
        this.repo = repo;
        map = new TreeMap<>();
    }

    @Override
    public byte[] serialize() {
        return serializeGitKeyValue(map).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String serializeString() {
        return serializeGitKeyValue(map);
    }

    @Override
    public void deserialize(byte[] data) {
        deserializeGitKeyValue(data, map, 0);
    }

    public void printCommit(String myHash) {
        System.out.println(YELLOW_BOLD + "commit " + myHash + RESET);
        System.out.println("Author: " + map.get("author"));
        if (map.containsKey("parent")) System.out.println("Parent: " + map.get("parent"));
        System.out.println(map.get(""));
    }

    public static String createCommit(GitRepository repo, String newTreeHash, String msg, boolean actualCommit) {
        var newCommit = new GitCommit(repo);
        newCommit.map.put("tree", newTreeHash);

        // Find parent, hard code main path since we're not allow to change it
        var lastCommitFile = repo.getRepoFilePath(Path.of("refs", "heads", "main"), false);
        if (lastCommitFile.toFile().exists()) {
            try {
                var lastCommit = Files.readString(lastCommitFile).replaceAll("\n", "");
                newCommit.map.put("parent", lastCommit);

                // Check if previous commit has the same tree, means we have nothing to update
                var lastCommitParent = (GitCommit) readGitObject(repo, newCommit.map.get("parent"));
                if (lastCommitParent.map.get("tree").equals(newTreeHash)) {
                    printLog("Nothing to commit to", MsgLevel.INFO);
                    return null;
                }

            } catch (IOException e) {
                printLog("Error when trying to read commit file", MsgLevel.ERROR);
                e.printStackTrace();
                return null;
            }
        }

        // find author
        var name = repo.getConfig("user", "name");
        var email = repo.getConfig("user", "email");
        var author = name == null ? "" : name;
        author += email == null ? author : author.isEmpty() ? email : " " + email;

        if (!author.isEmpty())
            newCommit.map.put("author", author);

        newCommit.map.put("", "\n" + msg + "\n");
        var newHash = writeGitObject(newCommit, actualCommit);

        // Update head file
        if (actualCommit) {
            try {
                Files.writeString(lastCommitFile, newHash);
            } catch (IOException e) {
                printLog("Error when trying to write to head file", MsgLevel.ERROR);
                e.printStackTrace();
                return null;
            }
        }

        return newHash;
    }
}
