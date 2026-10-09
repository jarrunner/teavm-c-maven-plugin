package jarrunner.teavmc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompileMojoTest {
    @Test
    void commandOrder() {
        var m = new CompileMojo();
        m.source = new File("target/c/all.c");
        m.args = List.of("-Oz", "-s");
        m.includes = List.of(new File("bindings"));
        m.libraries = List.of("winhttp", "bcrypt");
        m.callbackFlag = true;
        var t = new Target().id("x").compiler("cc").output("out.exe").args(List.of("-flto"));
        assertEquals(List.of("cc", "-Oz", "-s", "-flto", "-I", "bindings", CompileMojo.CALLBACK_FLAG,
                "-o", "out.exe", new File("target/c/all.c").getPath(), "-lwinhttp", "-lbcrypt"), m.command(t));
    }

    @Test
    void callbackFlagCanBeLeftOut() {
        var m = new CompileMojo();
        m.source = new File("all.c");
        m.callbackFlag = false;
        var t = new Target().id("x").compiler("cc").output("a");
        assertEquals(List.of("cc", "-o", "a", "all.c"), m.command(t));
    }

    @Test
    void failingCompilerIsReportedWithItsOutput() throws Exception {
        var log = Files.createTempFile("teavmc", ".log").toFile();
        var exit = Run.logged(List.of("no-such-compiler-teavmc"), new File("."), log);
        assertNotEquals(0, exit);
        assertTrue(Run.tail(log, 5).getFirst().contains("no-such-compiler-teavmc"));
    }
}
