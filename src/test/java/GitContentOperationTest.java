import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitContentOperationTest {
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

        // Make few files
        try {
            Files.createDirectories(Path.of("dir/test"));
            var file1 = Files.createFile(Path.of("hello.txt"));
            var file2 = Files.createFile(Path.of("someText.txt"));
            var file3 = Files.createFile(Path.of("dir/testsomeTextxx.txt"));
            var file4 = Files.createFile(Path.of("dir/nested1.txt"));

            Files.writeString(file1, "hello");
            Files.writeString(file2, "hfkjahsdljfh laskjhdf kasd");
            Files.writeString(file3, "eioiausodf aosdfasdf asdf");
            Files.writeString(file4, "lajhskjhf ajskdhfj lashdfjk hasfsajhdfk jahsdjfkashdkfj hasdf sjk");
        } catch (IOException e) {
            System.out.println("Cannot create files");
            e.printStackTrace();
        }
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

    @Test
    void testCommit() {

    }
}
