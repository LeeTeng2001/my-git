package git;

import org.ini4j.Wini;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import static helper.Utility.MsgLevel;
import static helper.Utility.printLog;

public final class GitRepository {
    Path workTree;
    Path gitDir;
    Wini config;
    HashSet<String> ignore;

    public GitRepository(Path root) {
        workTree = root;
        gitDir = workTree.resolve(".git");

        // Read config file
        File configFile = gitDir.resolve("config").toFile();
        File ignoreFile = workTree.resolve(".gitignore").toFile();

        try {
            config = new Wini(configFile);
        } catch (IOException ignored) {}

        ignore = new HashSet<>();
        ignore.add(".git");
        try {
            // Add ignore file if it exists
            var fis = new FileInputStream(ignoreFile);
            var scanner = new Scanner(fis);
            while (scanner.hasNextLine())
                ignore.add(scanner.nextLine());
            scanner.close();
        }
        catch(IOException ignored) {};
    }

    public Path getRelativeWorkingDir() {
        var absCurPath = Path.of("").toAbsolutePath();
        if (absCurPath.equals(workTree)) return Path.of(".");
        return absCurPath.relativize(workTree);
    }

    public String getConfig(String nameSpace, String key) {
        return config.get(nameSpace, key);
    }

    // Return a relative path from git from absolute path, useful when you want only relative path output
    public Path getRelativePath(Path absPath) {
        return gitDir.relativize(absPath);
    }

    // Return path under git directory from original path
    public Path getRepoPath(Path originalPath) {
        return gitDir.resolve(originalPath);
    }

    // Same as getRepoPath but will mkdir parent folder if specified, need absolute path to avoid null pointer
    public Path getRepoFilePath(Path originalPath, boolean mkdir) {
        if (originalPath.getParent() == null)
            return getRepoPath(originalPath);  // root file
        var parentFolder = getRepoDirPath(originalPath.getParent(), mkdir);

        if (parentFolder == null) {
            printLog("Error when checking parent folder: " + originalPath, MsgLevel.ERROR);
            return null;
        }

        return getRepoPath(originalPath);
    }

    // Same as getRepoPath but will mkdir if specified
    public Path getRepoDirPath(Path originalPath, boolean mkdir) {
        var path = getRepoPath(originalPath);
        var file = path.toFile();

        if (file.exists()) {
            if (file.isDirectory()) return path;
            else {
                printLog("Not a directory: " + path, MsgLevel.ERROR);
                return null;
            }
        }

        // File not exist
        if (mkdir) {
            try {
                Files.createDirectories(path);
                return path;
            } catch (IOException e) {
                printLog("Error when trying to create directory: " + path, MsgLevel.ERROR);
                e.printStackTrace();
                return null;
            }
        }

        printLog("Directory doesn't exist and mkdir=false: " + originalPath, MsgLevel.WARNING);
        return null;
    }

    // create git repository, return status code
    public static int createGitRepo(String target, String name, String email) {
        var createPath = new File(target);

        // make sure directory empty or not exist before proceeding
        if (createPath.exists()) {
            if (createPath.isDirectory() && createPath.list().length != 0) {
                printLog("Directory is not empty: " + target, MsgLevel.ERROR);
                return 1;
            }
            else if (createPath.isFile()){
                printLog("Target directory is a file: " + target, MsgLevel.ERROR);
                return 1;
            }
        }

        // Create necessary git directory
        try {
            GitRepository newRepo = new GitRepository(createPath.toPath());
            Files.createDirectories(newRepo.workTree);
            Files.createDirectories(newRepo.gitDir);

            // Necessary Folders
            Files.createDirectories(newRepo.getRepoDirPath(Path.of("branches"), true));
            Files.createDirectories(newRepo.getRepoDirPath(Path.of("objects"), true));
            Files.createDirectories(newRepo.getRepoDirPath(Path.of("refs/tags"), true));
            Files.createDirectories(newRepo.getRepoDirPath(Path.of("refs/heads"), true));

            // Config files ------------------
            // .git/description
            List<String> content = List.of("Unnamed repository; edit this file 'description' to name the repository.\n");
            Files.write(newRepo.getRepoFilePath(Path.of("description"), true), content, StandardCharsets.UTF_8);

            // .git/HEAD
            content = List.of("ref: refs/heads/main");
            Files.write(newRepo.getRepoFilePath(Path.of("HEAD"), true), content, StandardCharsets.UTF_8);

            // .git/config, hard coded the content
            content = Arrays.asList("[core]",
                                    "\trepositoryformatversion = 0",
                                    "\tfilemode = false",
                                    "\tbare = false",
                                    "\tignorecase = true",
                                    "[user]",
                                    "\tname = " + name,
                                    "\temail = " + email
            );
            Files.write(newRepo.getRepoFilePath(Path.of("config"), true), content, StandardCharsets.UTF_8);

            printLog("Successfully initialized git directory at: " + target, MsgLevel.SUCCESS);
        } catch (IOException e) {
            printLog("Error when trying to create directory for git", MsgLevel.ERROR);
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    // Recursively find git repository work directory
    public static GitRepository findGitRepo(Path from) {
        if (from == null) {
            printLog("Parent git repo doesn't exist!", MsgLevel.ERROR);
            return null;
        }

        var absFrom = from.toAbsolutePath();
        var gitDir = absFrom.resolve(".git");
        if (gitDir.toFile().isDirectory())
            return new GitRepository(absFrom);

        return findGitRepo(from.getParent());
    }

    // Convenient overloading method
    public static GitRepository findGitRepo() {
        var absPath = Path.of("").toAbsolutePath();
        return findGitRepo(absPath);
    }
}
