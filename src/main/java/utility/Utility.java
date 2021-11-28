package utility;

import git.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Utility {
    public enum MsgLevel {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
    }

    // Terminal output colour
    public static final String RESET = "\033[0m";
    static final String RED = "\033[0;31m";
    static final String GREEN = "\033[0;32m";
    static final String YELLOW = "\033[0;33m";
    static final String BLUE = "\033[0;34m";
    public static final String YELLOW_BOLD = "\033[1;33m";

    public static void printLog(String msg, MsgLevel level) {
        switch (level) {
            case INFO -> System.out.println(BLUE + "[Info] " + msg + RESET);
            case SUCCESS -> System.out.println(GREEN + "[Success] " + msg + RESET);
            case WARNING -> System.out.println(YELLOW + "[Warning] " + msg + RESET);
            case ERROR -> System.out.println(RED + "[Error] " + msg + RESET);
        }
    }

    public static int indexOfByte(byte[] array, byte target, int start) {
        for (int i = start; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOfByte(byte[] array, byte target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static GitObject readGitObject(GitRepository repo, String hash) {
        if (hash.length() != 40) {
            printLog("Hash length mismatch, must equal to 40: " + hash.length(), MsgLevel.ERROR);
            return null;
        }

        var objPath = Path.of("objects", hash.substring(0, 2), hash.substring(2));
        objPath = repo.getRepoFilePath(objPath, false);

        if (!objPath.toFile().exists() || objPath.toFile().isDirectory()) {
            printLog("Git object file doesn't exist or is a directory: " + objPath, MsgLevel.ERROR);
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
            var fmtSeparator  = indexOfByte(data, (byte) ' ');
            var format = new String(data, 0, fmtSeparator);

            var contentLenSeparator = indexOfByte(data, (byte) '\0');
            var contentLen = Integer.parseInt(new String(data, fmtSeparator + 1, contentLenSeparator - fmtSeparator - 1));
            var content = Arrays.copyOfRange(data, contentLenSeparator + 1, data.length);

            if (contentLen != content.length) {
                printLog("Git content length mismatch: " + contentLen + " != " + content.length, MsgLevel.ERROR);
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
            printLog("Cannot find file in git repo: " + objPath, MsgLevel.ERROR);
            e.printStackTrace();
            return null;
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    // We can choose to compute the hash without writing
    public static String writeGitObject(GitObject object, Boolean write) {
        var data = object.serialize();
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
                printLog("Error when trying to write to file: " + writePath, MsgLevel.ERROR);
                e.printStackTrace();
            }
        }

        return sha1;
    }

    // Parse key-value file format used by commit and tag
    public static void deserializeGitKeyValue(byte[] rawContent, TreeMap<String, String> map, int startPos) {
        // Tag and commit share the same file format
        var space = indexOfByte(rawContent, (byte)' ', startPos);
        var newLine = indexOfByte(rawContent, (byte)'\n', startPos);

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
                    writer.write(obj.serialize());
                } catch (IOException e) {
                    printLog("Error when trying to write to: " + objPath, MsgLevel.ERROR);
                    e.printStackTrace();
                    return;
                }
            }
            else {
                printLog("Format not supported for: " + leaf.fmt, MsgLevel.WARNING);
            }
        }
    }
}
