package jarrunner.teavmc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/** Runs a command with its output (stdout and stderr together) written to a log file. */
final class Run {
    private Run() {}

    static int logged(List<String> cmd, File dir, File log) {
        try {
            log.getParentFile().mkdirs();
            var p = new ProcessBuilder(cmd).directory(dir).redirectErrorStream(true)
                    .redirectOutput(log).start();
            return p.waitFor();
        } catch (IOException e) {
            write(log, "cannot run " + cmd.getFirst() + ": " + e.getMessage());
            return -1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    static List<String> tail(File log, int n) {
        try {
            var lines = Files.readAllLines(log.toPath(), StandardCharsets.UTF_8);
            return lines.subList(Math.max(0, lines.size() - n), lines.size());
        } catch (IOException e) {
            return List.of("(no output: " + e.getMessage() + ")");
        }
    }

    private static void write(File log, String s) {
        try { Files.writeString(log.toPath(), s); } catch (IOException ignored) {}
    }
}
