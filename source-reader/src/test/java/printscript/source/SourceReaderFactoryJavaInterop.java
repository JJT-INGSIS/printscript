package printscript.source;

import java.io.InputStream;
import java.nio.file.Path;

final class SourceReaderFactoryJavaInterop {

    private SourceReaderFactoryJavaInterop() {
    }

    static void consumeFactoryFromJava(InputStream inputStream, Path path) {
        SourceReaderFactory.fromString("");
        SourceReaderFactory.fromInputStream(inputStream);
        SourceReaderFactory.fromPath(path);
    }
}
