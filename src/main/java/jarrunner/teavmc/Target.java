package jarrunner.teavmc;

import java.util.*;
import datapotter.datahelper.Data;

/** One native build of the generated C: which compiler, which extra arguments, which output file. */
@Data
public final class Target extends Target_A {
    String id;
    String compiler;
    String output;
    List<String> args = new ArrayList<>();
}
