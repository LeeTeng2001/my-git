package helper;

import git.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Function {
    public static GitObject readGitObject(GitRepository repo, String hash) {
        if (hash == null) {  // from name resolve function
            Utility.printLog("Hash is null before reading git object ", Utility.MsgLevel.ERROR);
            return null;
        }

        var objPath = Path.of("objects", hash.substring(0, 2), hash.substring(2));
        objPath = repo.getRepoFilePath(objPath, false);

        if (!objPath.toFile().exists() || objPath.toFile().isDirectory()) {
            Utility.printLog("Git object file doesn't exist or is a directory: " + objPath, Utility.MsgLevel.ERROR);
            return null;
        }

        try {
            // Decompress git object using zlib
            var file = new FileInputStream(objPath.toFile());
            var inflator = new Inflater();
            inflator.setInput(file.readAllBytes());  // TODO: Fix read all bytes!!!!!
            var out = new ByteArrayOutputStream();
            var buffer = new byte[10];
            while (!inflator.finished()) {
                int len = inflator.inflate(buffer);
                out.write(buffer, 0, len);
            }

            // Process git data according to specification, note to self, DON'T use STRING for TREE data!!!!  ------------
            var data = out.toByteArray();
            var fmtSeparator  = Utility.indexOfByte(data, (byte) ' ');
            var format = new String(data, 0, fmtSeparator);

            var contentLenSeparator = Utility.indexOfByte(data, (byte) '\0');
            var contentLen = Integer.parseInt(new String(data, fmtSeparator + 1, contentLenSeparator - fmtSeparator - 1));
            var content = Arrays.copyOfRange(data, contentLenSeparator + 1, data.length);

            if (contentLen != content.length) {
                Utility.printLog("Git content length mismatch: " + contentLen + " != " + content.length, Utility.MsgLevel.ERROR);
                return null;
            }

            // Return git object depending on its type
            return switch (format) {
                case "blob" -> new GitBlob(repo, content);
                case "commit" -> new GitCommit(repo, content);
                case "tag" -> new GitTag(repo, content);
                case "tree" -> new GitTree(repo, content);
                default -> null;
            };

        } catch (FileNotFoundException e) {
            Utility.printLog("Cannot find file in git repo: " + objPath, Utility.MsgLevel.ERROR);
            e.printStackTrace();
            return null;
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    // We can choose to compute the hash without writing
    public static String writeGitObject(GitObject object, Boolean write) {
        var data = object.serializeString();
        // Add header and necessary format, don't need StringBuilder because compiler will optimize it anyway
        var finalData = object.format + " " + data.length() + "\0" + data;
        // Hash value
        var sha1 = DigestUtils.sha1Hex(finalData);

        if (write) {
            // Write data to git
            var writePath = object.repo.getRepoFilePath(Path.of("objects", sha1.substring(0, 2), sha1.substring(2)), true);

            // Compress using zlib, utf-8
            var bytes = finalData.getBytes(StandardCharsets.UTF_8);
            var deflater = new Deflater();
            deflater.setInput(bytes);

            // Write to output
            try (FileOutputStream outputStream = new FileOutputStream(writePath.toFile()))  {
                deflater.finish();
                byte[] buffer = new byte[1000];
                while (!deflater.finished())  {
                    int count = deflater.deflate(buffer);
                    outputStream.write(buffer, 0, count);
                }
            } catch (IOException e) {
                Utility.printLog("Error when trying to write to file: " + writePath, Utility.MsgLevel.ERROR);
                e.printStackTrace();
            }
        }

        return sha1;
    }

    // Parse key-value file format used by commit and tag
    public static void deserializeGitKeyValue(byte[] rawContent, TreeMap<String, String> map, int startPos) {
        // Tag and commit share the same file format
        var space = Utility.indexOfByte(rawContent, (byte)' ', startPos);
        var newLine = Utility.indexOfByte(rawContent, (byte)'\n', startPos);

        // Base case
        if (space == -1 || space > newLine) {
            map.put("", new String(rawContent, startPos, rawContent.length - startPos));
            return;
        }

        var key = new String(rawContent, startPos, space - startPos);
        var value = new String(rawContent, space + 1, newLine - space - 1);
        map.put(key, value);

        // Recursive
        deserializeGitKeyValue(rawContent, map, newLine + 1);
    }

    // Serialize key-value object to git file format
    public static String serializeGitKeyValue(TreeMap<String, String> map) {
        StringBuilder result = new StringBuilder();

        for (var key : map.descendingKeySet()) {
            result.append(key);
            result.append(' ');
            result.append(map.get(key));
            result.append('\n');
        }

        return result.toString();
    }

    // Recursively create original object from git tree
    public static void reconstructTree(GitRepository repo, GitTree tree, Path path) {
        for (var leaf: tree.leaves) {
            var obj = readGitObject(repo, leaf.sha);
            var objPath = path.resolve(leaf.path);

            if (leaf.fmt.equals("tree")) {
                objPath.toFile().mkdirs();
                reconstructTree(repo, (GitTree) obj, objPath);
            }
            else if (leaf.fmt.equals("blob")) {
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(objPath.toString()), StandardCharsets.UTF_8))) {
                    writer.write(obj.serializeString());
                } catch (IOException e) {
                    Utility.printLog("Error when trying to write to: " + objPath, Utility.MsgLevel.ERROR);
                    e.printStackTrace();
                    return;
                }
            }
            else {
                Utility.printLog("Format not supported for: " + leaf.fmt, Utility.MsgLevel.WARNING);
            }
        }
    }

    private static String resolveRef(GitRepository repo, Path filePath) {
        // Reference file is always in ASCII / UTF-8 compatible, strip newline
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8).replace("\n", "");
//            printLog("Reference file content: " + content, MsgLevel.INFO);

            // Check for indirect reference
            if (content.startsWith("ref: "))
                return resolveRef(repo, repo.getRepoFilePath(Path.of(content.substring(5)), false));
            return content;

        } catch (IOException e) {
            Utility.printLog("Cannot read file: " + filePath, Utility.MsgLevel.ERROR);
            e.printStackTrace();
            return null;
        }
    }

    private static void getAllRefs(GitRepository repo, Path gitDirPath, TreeMap<String, String> res) {
        var children = gitDirPath.toFile().listFiles();
        if (children == null) return;

        for (var file: children) {
            if (file.isDirectory()) {
                getAllRefs(repo, file.toPath(), res);
            }
            else {
                res.put(file.toString(), resolveRef(repo, file.toPath()));
            }
        }
    }

    public static TreeMap<String, String> getAllRefs(GitRepository repo) {
        // Ordered map because we want to maintain insertion order
        var res = new TreeMap<String, String>();
        var refRoot = repo.getRepoDirPath(Path.of("refs"), false);
        getAllRefs(repo, refRoot, res);
        return res;
    }

    // Git-like resolve name feature, return object hash
    private static ArrayList<String> hashNameResolve(GitRepository repo, String name) {
        var candidates = new ArrayList<String>();

        // Check special case first
        if (name.isEmpty()) {
            return candidates;
        }
        else if (name.equals("HEAD")) {
            candidates.add(resolveRef(repo, repo.getRepoFilePath(Path.of("HEAD"), false)));
            return candidates;
        }
        else if (name.length() == 40) {
            candidates.add(name);
            return candidates;  // Very crude hash check by length
        }
        else if (name.length() > 40 || name.length() < 4) {  // minimum and maximum hash length
            return candidates;
        }

        // Otherwise, find matching name
        var hashFolderPath = repo.getRepoPath(Path.of("objects", name.substring(0, 2))).toFile();
        if (hashFolderPath.isDirectory()) {  // exist and has a directory
            var candidatePrefix = name.substring(2);
            var files = hashFolderPath.listFiles();
            if (files == null) return candidates;

            // If file name start with our target prefix, add this file to candidate
            for (var file: files) {
                if (file.getName().startsWith(candidatePrefix)) {
                    candidates.add(hashFolderPath.getName() + file.getName());
                }
            }
        }

        return candidates;
    }

    // Public api, will check hash and tags
    public static String fuzzyNameMatch(GitRepository repo, String candidate) {
        var candidates = hashNameResolve(repo, candidate);
        if (candidates.size() > 1) {
            Utility.printLog("Has more than one candidates, ambiguous hash: ", Utility.MsgLevel.ERROR);
            for (var item : candidates) {
                System.out.println(item);
            }
        }
        else if (candidates.size() == 1)
            return candidates.get(0);

        // Check for references
        var allReferences = getAllRefs(repo);
        for (var key : allReferences.descendingKeySet()) {
            if (Path.of(key).toFile().getName().equals(candidate)) {
                return resolveRef(repo, Path.of(key));
            }
        }

        // No match
        return null;
    }

}
