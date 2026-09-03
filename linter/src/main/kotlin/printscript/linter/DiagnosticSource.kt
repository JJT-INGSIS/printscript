package printscript.linter

public interface DiagnosticSource {

    public fun nextDiagnostic(): DiagnosticReadResult
}
