# teavm-c-maven-plugin

Compiles the C that [TeaVM](https://teavm.org)'s C backend generates into native executables, one per target, as part of `mvn package`. No build scripts.

TeaVM's own `teavm-maven-plugin` (with `<targetType>C</targetType>`) turns your classes into `all.c`. This plugin takes it from there: for each target it runs the compiler you name (clang, a target-prefixed clang such as llvm-mingw's `aarch64-w64-mingw32-clang`, `zig cc`, gcc) with the arguments, include folders and libraries you configure, and writes each compiler's full output to `target/teavmc-<id>.log`.

## Usage

```xml
<plugin>
  <groupId>io.github.jarrunner</groupId>
  <artifactId>teavm-c-maven-plugin</artifactId>
  <version>1.0.0</version>
  <executions>
    <execution>
      <goals><goal>compile</goal></goals>
      <configuration>
        <includes><include>native/include</include></includes>
        <args><arg>-Oz</arg><arg>-flto</arg><arg>-Wl,--gc-sections</arg><arg>-s</arg></args>
        <libraries><library>winhttp</library></libraries>
        <targets>
          <target><id>x86_64</id><compiler>x86_64-w64-mingw32-clang</compiler><output>dist/app-x86_64.exe</output></target>
          <target><id>arm64</id><compiler>aarch64-w64-mingw32-clang</compiler><output>dist/app-arm64.exe</output></target>
        </targets>
      </configuration>
    </execution>
  </executions>
</plugin>
```

The goal binds to `package`. Each target's command is: compiler, `args`, the target's own `args`, `-I` for each include, `-Wno-error=incompatible-function-pointer-types`, `-o <output> <source>`, then `-l` for each library.

- `source`: the generated C, default `${project.build.directory}/c/all.c` (set TeaVM's `targetDirectory` to `${project.build.directory}/c`).
- `callbackFlag` (default `true`): adds `-Wno-error=incompatible-function-pointer-types`. TeaVM passes a Java method as a C callback typed `void*` where the C typedef names a specific pointer type; the ABI is the same, and clang 16 and later reject it as an error without this flag.
- A target's `output` is relative to the project folder unless absolute. Its `args` come after the shared ones, e.g. `<arg>-target</arg><arg>x86_64-linux-gnu</arg>` for `zig cc`.
- `-Dteavmc.targets=x86_64,arm64` builds only those targets; `-Dteavmc.skip=true` skips the goal.

A complete setup, with Windows (llvm-mingw, two architectures) and Linux (zig cc, cross-compiled) profiles, is [jr](https://github.com/jarrunner/jr)'s `jr/pom.xml`.

## Licence

Apache License 2.0.
