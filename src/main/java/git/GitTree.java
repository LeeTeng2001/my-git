package git;

import helper.Utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import static helper.Function.readGitObject;
import static helper.Function.writeGitObject;
import static helper.Utility.indexOfByte;
import static helper.Utility.printLog;

public final class GitTree extends GitObject {
    public ArrayList<Leaf> leaves;

    public GitTree(GitRepository repo, byte[] data) {
        this.repo = repo;
        this.format = "tree";
        deserialize(data);
    }

    public GitTree(GitRepository repo, ArrayList<Leaf> leaves) {
        this.repo = repo;
        this.format = "tree";
        this.leaves = leaves;
    }

    @Override
    public byte[] serialize() {
        return serializeTree(leaves);
    }

    @Override
    public String serializeString() {
        return new String(serializeTree(leaves));
    }

    @Override
    public void deserialize(byte[] data) {
        leaves = deserializeTree(data);
    }

    public static final class Leaf {
        public String mode;
        public String path;
        public String sha;
        public String fmt;

        public Leaf(String mode, String path, String sha, String fmt) {
            this.mode = mode;
            this.path = path;
            this.sha = sha;
            this.fmt = fmt;
        }

        public String getFmtOutput() {
            return String.format("%6s", mode).replace(' ', '0') + " "  + fmt + " " + sha + "\t" + path;
        }

        // For sorting
        public String getPath() {
            return path;
        }
    }

    private static Leaf deserializeSingleTree(byte[] raw, AtomicInteger atomicStartPos, GitRepository repo) {
        int startPos = atomicStartPos.get();

        // Get mode
        var modeTerminator = indexOfByte(raw, (byte) ' ', startPos);
        var mode = new String(raw, startPos, modeTerminator - startPos);

        // get path
        var pathTerminator = indexOfByte(raw, (byte) '\0', modeTerminator);
        var path = new String(raw, modeTerminator + 1, pathTerminator - modeTerminator - 1);

        // Get fixed 20 bytes sha stream (binary encoded) and turn into hex string
        var hexStream = new StringBuilder();
        for (int i = pathTerminator + 1; i < pathTerminator + 21; i++) {
            var chByte = raw[i];

            int hexInt = 0;
            if (((1 << 4) & chByte) != 0) hexInt += 1;
            if (((1 << 5) & chByte) != 0) hexInt += 2;
            if (((1 << 6) & chByte) != 0) hexInt += 4;
            if (((1 << 7) & chByte) != 0) hexInt += 8;
            char hexChar = (char) (hexInt >= 10 ? 'a' + hexInt - 10 : '0' + hexInt);
            hexStream.append(hexChar);

            hexInt = 0;
            if ((1 & chByte) != 0) hexInt += 1;
            if (((1 << 1) & chByte) != 0) hexInt += 2;
            if (((1 << 2) & chByte) != 0) hexInt += 4;
            if (((1 << 3) & chByte) != 0) hexInt += 8;
            hexChar = (char) (hexInt >= 10 ? 'a' + hexInt - 10 : '0' + hexInt);
            hexStream.append(hexChar);
        }

        var hexString = hexStream.toString();
        var gitObj = readGitObject(repo, hexString);

        atomicStartPos.set(pathTerminator + 21);
        return new Leaf(mode, path, hexString, gitObj.format);
    }

    private static ArrayList<Leaf> deserializeTree(byte[] raw) {
        var curPos = new AtomicInteger(0);
        var repo = GitRepository.findGitRepo();
        var leaves = new ArrayList<Leaf>();

        while (curPos.get() < raw.length) {
            leaves.add(deserializeSingleTree(raw, curPos, repo));
        }

        return leaves;
    }

    private static byte[] serializeTree(ArrayList<Leaf> leaves) {
            var res = new ByteArrayOutputStream();

            try {
                for (var leaf : leaves) {
                    res.write(leaf.mode.getBytes(StandardCharsets.UTF_8));
                    res.write((byte) ' ');
                    res.write(leaf.path.getBytes(StandardCharsets.UTF_8));
                    res.write((byte) '\0');
                    for (int i = 0; i < leaf.sha.length(); i += 2) {
                        byte curByte = 0;
                        int curVal = Character.digit(leaf.sha.charAt(i), 16);
                        if ((1 & curVal) != 0) curByte |= 1 << 4;
                        if (((1 << 1) & curVal) != 0) curByte |= 1 << 5;
                        if (((1 << 2) & curVal) != 0) curByte |= 1 << 6;
                        if (((1 << 3) & curVal) != 0) curByte |= 1 << 7;

                        curVal = Character.digit(leaf.sha.charAt(i + 1), 16);
                        if ((1 & curVal) != 0) curByte |= 1;
                        if (((1 << 1) & curVal) != 0) curByte |= 1 << 1;
                        if (((1 << 2) & curVal) != 0) curByte |= 1 << 2;
                        if (((1 << 3) & curVal) != 0) curByte |= 1 << 3;
                        res.write(curByte);
                    }
                    // NO NEWLINE AT THE END!
                }
            } catch (IOException e) {
                printLog("Error when trying to write string to byte", Utility.MsgLevel.ERROR);
                e.printStackTrace();
            }

            return res.toByteArray();
        }

    // Currently, all object has the same non-executable mode
    private static Leaf blobPathToLeafAndCommit(GitRepository repo, Path objPath, boolean commitMode, boolean commit) {
        byte[] content;
        try {
            content = Files.readAllBytes(objPath);
        } catch (IOException e) {
            printLog("Error when trying to read object data", Utility.MsgLevel.ERROR);
            e.printStackTrace();
            return null;
        }
        var blob = new GitBlob(repo, content);
        String sha = writeGitObject(blob, false);

        if (commitMode) {  // only check for commit in commit mode
            // Check if the file is new
            var gitFilePath = repo.getRepoPath(Path.of("objects", sha.substring(0, 2), sha.substring(2)));
            if (!gitFilePath.toFile().exists()) {
                if (commit) {
                    writeGitObject(new GitBlob(repo, content), true);
                    printLog("Committing new file: " + objPath, Utility.MsgLevel.SUCCESS);
                }
                else printLog("Committing new file: " + objPath, Utility.MsgLevel.INFO);
            }
        }

        var mode = objPath.toFile().canExecute() ? "100755" : "100644";

        var leaf = new Leaf(mode, objPath.toFile().getName(), sha, "blob");
        return leaf;
    }

    public static Leaf commitFromPath(GitRepository repo, Path objPath, boolean commit) {
        var children = objPath.toFile().listFiles();
        if (children == null) return null;  // cannot have empty directory
        var leaves = new ArrayList<Leaf>();

        for (File child : children) {
            if (repo.ignore.contains(child.getName())) continue;  // skip ignored file

            if (child.isDirectory()) {
                var childTreeLeaf = commitFromPath(repo, child.toPath(), commit);
                if (childTreeLeaf == null) continue;  // skip empty dir
                leaves.add(childTreeLeaf);
            }
            else leaves.add(blobPathToLeafAndCommit(repo, child.toPath(), true, commit) );
        }

        // Base case, empty valid leaves
        if (leaves.isEmpty()) return null;

        // Leaves need to be sorted!
        leaves.sort(Comparator.comparing(Leaf::getPath));

        // Commit if needed
        var sha = writeGitObject(new GitTree(repo, leaves), false);

        // Check if the directory is new
        var gitFilePath = repo.getRepoPath(Path.of("objects", sha.substring(0, 2), sha.substring(2)));
        if (!gitFilePath.toFile().exists()) {
            if (commit) {
                writeGitObject(new GitTree(repo, leaves), true);
                printLog("Committing new directory: " + objPath, Utility.MsgLevel.SUCCESS);
            }
            else printLog("Committing new directory: " + objPath, Utility.MsgLevel.INFO);
        }

        // REMEMBER there's no padding 0 in front of the code!!!!
        return new Leaf("40000", objPath.toFile().getName(), sha, "tree");
    }

    // Special blob leaves with relative path from root
    public static ArrayList<Leaf> getNewUncommittedLeaves(GitRepository repo, Path objPath) {
        var children = objPath.toFile().listFiles();
        if (children == null) return null;  // cannot have empty directory
        var leaves = new ArrayList<Leaf>();

        for (File child : children) {
            if (repo.ignore.contains(child.getName())) continue;  // skip ignored file

            if (child.isDirectory()) {
                var nextLevel = getNewUncommittedLeaves(repo, child.toPath());
                if (nextLevel != null)
                    leaves.addAll(nextLevel);  // add all children new tree
            }
            else {
                var blobLeaf = blobPathToLeafAndCommit(repo, child.toPath(), false, false);
                var gitFilePath = repo.getRepoPath(Path.of("objects", blobLeaf.sha.substring(0, 2), blobLeaf.sha.substring(2)));
                if (!gitFilePath.toFile().exists()) {
                    blobLeaf.path = child.toString();
                    leaves.add(blobLeaf);
                }
            }
        }

        return leaves.isEmpty() ? null : leaves;
    }
}
