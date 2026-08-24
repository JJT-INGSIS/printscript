package printscript.linter

public interface DiagnosticSource {

    /**
     * Lee el próximo diagnóstico.
     *
     * Failure y EndOfInput son resultados terminales.
     */
    public fun nextDiagnostic(): DiagnosticReadResult
}
