package git;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static helper.Function.readGitObject;
import static helper.Utility.indexOfByte;

public class GitTree extends GitObject {
    public ArrayList<Leaf> leaves;

    public GitTree(GitRepository repo, byte[] data) {
        this.format = "tree";
        deserialize(data);
    }

    @Override
    public String serialize() {
        return serializeTree(leaves);
    }

    @Override
    public void deserialize(byte[] data) {
        leaves = deserializeTree(data);
    }

    public static class Leaf {
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

    private static String serializeTree(ArrayList<Leaf> leaves) {
            var res = new StringBuilder();

            for (var leaf : leaves) {
                res.append(leaf.mode);
                res.append(' ');
                res.append(leaf.path);
                res.append('\0');
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
                    res.append(curByte);
                }
                res.append('\n');
            }

            return res.toString();
        }
}
