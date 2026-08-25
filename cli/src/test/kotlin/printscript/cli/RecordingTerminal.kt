package printscript.cli

import printscript.cli.internal.io.Terminal

/**
 * Terminal de prueba que guarda por separado lo que va a la salida
 * estándar y lo que va a la salida de errores.
 *
 * Sin esto habría que capturar `System.out`, que es global y hace que
 * los tests no se puedan correr en paralelo.
 */
internal class RecordingTerminal : Terminal {

    private val outputLines = mutableListOf<String>()
    private val errorLines = mutableListOf<String>()

    override fun writeLine(line: String) {
        outputLines.add(line)
    }

    override fun writeError(line: String) {
        errorLines.add(line)
    }

    fun output(): List<String> = outputLines.toList()

    fun errors(): List<String> = errorLines.toList()
}
