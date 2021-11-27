package git;

import java.util.TreeMap;

import static utility.Utility.*;

public class GitCommit extends GitObject {
    public TreeMap<String, String> map;

    public GitCommit(GitRepository repo, String data) {
        this.format = "commit";
        this.repo = repo;
        map = new TreeMap<>();
        deserialize(data);
    }

    @Override
    public String serialize() {
        return serializeGitKeyValue(map);
    }

    @Override
    public void deserialize(String data) {
        parseGitKeyValue(data, map, 0);
    }

    public void printCommit(String myHash) {
        System.out.println(YELLOW_BOLD + "commit " + myHash + RESET);
        System.out.println("Author: " + map.get("author"));
        System.out.println(map.get(""));
    }
}
