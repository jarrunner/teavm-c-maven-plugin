package jarrunner.teavmc;

import java.io.*;
import java.util.*;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Mojo;

/** Compiles TeaVM's generated C (all.c) into one native executable per target. */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public final class CompileMojo extends AbstractMojo {
    static final String CALLBACK_FLAG = "-Wno-error=incompatible-function-pointer-types";

    @Parameter(defaultValue = "${project.basedir}", readonly = true) File basedir;
    @Parameter(defaultValue = "${project.build.directory}", readonly = true) File buildDirectory;
    @Parameter(property = "teavmc.source", defaultValue = "${project.build.directory}/c/all.c") File source;
    @Parameter List<File> includes = new ArrayList<>();
    @Parameter List<String> args = new ArrayList<>();
    @Parameter List<String> libraries = new ArrayList<>();
    @Parameter List<Target> targets = new ArrayList<>();
    @Parameter(defaultValue = "true") boolean callbackFlag;
    @Parameter(property = "teavmc.targets") String only;
    @Parameter(property = "teavmc.skip", defaultValue = "false") boolean skip;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) { getLog().info("teavmc: skipped"); return; }
        if (!source.isFile()) throw new MojoExecutionException("no generated C at " + source + " (run TeaVM's C target first)");
        var wanted = only == null || only.isBlank() ? null : Set.of(only.split("\\s*,\\s*"));
        for (var t : targets)
            if (wanted == null || wanted.contains(t.id())) build(t);
    }

    void build(Target t) throws MojoExecutionException {
        var out = output(t);
        out.getParentFile().mkdirs();
        var cmd = command(t);
        var log = new File(buildDirectory, "teavmc-" + t.id() + ".log");
        getLog().info("teavmc: " + t.id() + " -> " + out);
        getLog().debug(String.join(" ", cmd));
        var exit = Run.logged(cmd, basedir, log);
        if (exit != 0) {
            Run.tail(log, 30).forEach(getLog()::error);
            throw new MojoExecutionException(t.compiler() + " failed for " + t.id() + " (exit " + exit + "), full output in " + log);
        }
        getLog().info("teavmc: " + t.id() + " " + out.length() + " bytes");
    }

    File output(Target t) {
        var f = new File(t.output());
        return f.isAbsolute() || basedir == null ? f : new File(basedir, t.output());
    }

    List<String> command(Target t) {
        var cmd = new ArrayList<String>();
        cmd.add(t.compiler());
        cmd.addAll(args);
        cmd.addAll(t.args());
        includes.forEach(i -> { cmd.add("-I"); cmd.add(i.getPath()); });
        if (callbackFlag) cmd.add(CALLBACK_FLAG);
        cmd.addAll(List.of("-o", t.output(), source.getPath()));
        libraries.forEach(l -> cmd.add("-l" + l));
        return cmd;
    }
}
