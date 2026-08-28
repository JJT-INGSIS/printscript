package printscript.linter

import printscript.model.source.SourceSpan

/**
 * Contrato de diagnóstico. Cada regla aporta los suyos: el motor los
 * transporta y nunca los interpreta.
 *
 * A diferencia de LexicalError o SemanticError, acá el núcleo no trae
 * ningún caso propio: el linter no despacha, hace fan-out, así que no
 * existe la sentencia que ninguna regla supo mirar.
 */
public interface Diagnostic {

    public val span: SourceSpan
}
