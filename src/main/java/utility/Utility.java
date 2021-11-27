package utility;

import git.GitBlob;
import git.GitObject;
import git.GitRepository;

import java.io.*;
import java.nio.file.Path;
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

    public static void PrintLog(String msg, MsgLevel level) {
        switch (level) {
            case INFO -> System.out.println(BLUE + "[Info] " + msg + RESET);
            case SUCCESS -> System.out.println(GREEN + "[Success] " + msg + RESET);
            case WARNING -> System.out.println(YELLOW + "[Warning] " + msg + RESET);
            case ERROR -> System.out.println(RED + "[Error] " + msg + RESET);
        }
    }

    public static GitObject readGitObject(GitRepository repo, String hash) {
        if (hash.length() != 40) {
            PrintLog("Hash length mismatch, must equal to 40: " + hash.length(), MsgLevel.ERROR);
            return null;
        }

        var objPath = Path.of("objects", hash.substring(0, 2), hash.substring(2));
        objPath = repo.getRepoFilePath(objPath, false);

        if (!objPath.toFile().exists() || objPath.toFile().isDirectory()) {
            PrintLog("Git object file doesn't exist or is a directory: " + objPath, MsgLevel.ERROR);
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
                PrintLog("Git content length mismatch: " + contentLen + " != " + content.length(), MsgLevel.ERROR);
                return null;
            }

            // Return git object depends on its type
            GitObject gitObject = null;
            if (format.equals("blob"))  {
                gitObject = new GitBlob(repo, content);
            }

            return gitObject;
        } catch (FileNotFoundException e) {
            PrintLog("Cannot find file in git repo: " + objPath, MsgLevel.ERROR);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
