package utility;

import git.GitBlob;
import git.GitObject;
import git.GitRepository;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;

public class Utility {
    public enum MsgLevel {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
    }

    // Terminal output colour
    static final String RESET = "\033[0m";
    static final String RED = "\033[0;31m";
    static final String GREEN = "\033[0;32m";
    static final String YELLOW = "\033[0;33m";
    static final String BLUE = "\033[0;34m";

    public static void printLog(String msg, MsgLevel level) {
        switch (level) {
            case INFO -> System.out.println(BLUE + "[Info] " + msg + RESET);
            case SUCCESS -> System.out.println(GREEN + "[Success] " + msg + RESET);
            case WARNING -> System.out.println(YELLOW + "[Warning] " + msg + RESET);
            case ERROR -> System.out.println(RED + "[Error] " + msg + RESET);
        }
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
            InputStream in = new InflaterInputStream(new FileInputStream(objPath.toFile()));
            OutputStream out = new ByteArrayOutputStream();
            var buffer = new byte[1000];
            int len;
            while((len = in.read(buffer)) > 0)
                out.write(buffer, 0, len);
            in.close();

            // Process git data according to specification ------------
            var data = out.toString();
            var fmtSeparator  = data.indexOf(' ');
            var format = data.substring(0, fmtSeparator);

            var contentLenSeparator = data.indexOf('\0', fmtSeparator);
            var contentLen = Integer.parseInt(data.substring(fmtSeparator + 1, contentLenSeparator));
            var content = data.substring(contentLenSeparator + 1);

            if (contentLen != content.length()) {
                printLog("Git content length mismatch: " + contentLen + " != " + content.length(), MsgLevel.ERROR);
                return null;
            }

            // Return git object depends on its type
            GitObject gitObject = null;
            // TODO: Consider more format
            if (format.equals("blob"))  {
                gitObject = new GitBlob(repo, content);
            }

            return gitObject;
        } catch (FileNotFoundException e) {
            printLog("Cannot find file in git repo: " + objPath, MsgLevel.ERROR);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
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
}
