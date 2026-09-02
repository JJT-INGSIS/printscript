package printscript.runtime

/** Output port used by statement executors without depending on a terminal or file. */
public interface ProgramOutput {

    public fun writeLine(line: String)
}
