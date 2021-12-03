import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


class GitInitTest {
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

        assertEquals(testingDir, testDir.toString());  // to avoid messing in the wrong directory
    }

    @AfterEach
    void tearDown() {
        app = null;
        cmd = null;

        // remove content for next test if we're done and in the correct directory
        try {
            if (testDir.toString().equals(testingDir))
                FileUtils.cleanDirectory(testDir);
        } catch (IOException e) {
            System.err.println("Error when trying to clean directory: " + testDir);
            e.printStackTrace();
        }
    }

    @Test
    void testWithoutSubCommand() {
        int exitCode = cmd.execute();
        assertNotEquals(0, exitCode);
    }

    @Test
    void testInitialiseBare() {
        int exitCode = cmd.execute("init");
        assertEquals(0, exitCode);
    }

    @Test
    void testInitialiseInformation() {
        int exitCode = cmd.execute("init", "hello", "bryan", "random@gmail.com");
        assertEquals(0, exitCode);
    }

    @Test
    void testNoParentRepo() {
        // Program should terminate if it executes in invalid git directory
        int exitCode = cmd.execute("cat-file", "some-hash");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("commit", "Some message");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("hash-object", "Some hash");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("log");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("ls-tree", "some-hash");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("restore", "xd", "xd");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("show-ref");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("status");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("tag");
        assertNotEquals(0, exitCode);
    }
}