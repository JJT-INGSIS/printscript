package printscript.source;

import java.io.InputStream;
import java.nio.file.Path;

/** Compilation fixture that protects the public factory API consumed from Java. */
final class SourceReaderFactoryJavaInterop {

    private SourceReaderFactoryJavaInterop() {
    }

    static void consumeFactoryFromJava(InputStream inputStream, Path path) {
        SourceReaderFactory.fromString("");
        SourceReaderFactory.fromInputStream(inputStream);
        SourceReaderFactory.fromPath(path);
    }
}
