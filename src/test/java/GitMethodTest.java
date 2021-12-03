import git.GitBlob;
import git.GitRepository;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Random;

import static helper.Function.*;
import static org.junit.jupiter.api.Assertions.*;

class GitMethodTest {
    private final static String testingDir = "/Users/lunafreya/Downloads/testing-dir";

    Git app;
    CommandLine cmd;
    StringWriter sw;
    File testDir = Path.of(System.getProperty("user.dir")).toFile();

    @BeforeEach
    void setUp() {
        app = new Git();
        cmd = new CommandLine(app);
        sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        assertEquals(testingDir, testDir.toString());
        cmd.execute("init");
    }

    @AfterEach
    void tearDown() {
        app = null;
        cmd = null;

        try {
            if (testDir.toString().equals(testingDir))
                FileUtils.cleanDirectory(testDir);
        } catch (IOException e) {
            System.err.println("Error when trying to clean directory: " + testDir);
            e.printStackTrace();
        }
    }

    @RepeatedTest(2)
    void testCompressAndDecompressWithRandomData() {
        // Mock an object, test for edge case like too big or 0 byte
        // an array of int max takes too long to create, we test from 0 byte up to 150mb
        var random = new Random();
        var testSize = new int[]{0, random.nextInt(1000), random.nextInt(10000), random.nextInt(100000), random.nextInt(1000000), random.nextInt(150000000)};

        for (var size: testSize){
            // Compression, mock a random object data
            var data = new byte[size];
            random.nextBytes(data);
            var curRepo = GitRepository.findGitRepo();
            var gitObject = new GitBlob(curRepo, data);
            var computedHash = writeGitObject(gitObject, true);  // write object

            // Decompression
            var absHash = fuzzyNameMatch(curRepo, computedHash);
            assertNotEquals(absHash, null);
            var obj = readGitObject(curRepo, absHash);
            var compareData = obj.serialize();
            assertNotEquals(compareData, null);
            assertArrayEquals(compareData, data);
        }
    }
}
