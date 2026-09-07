package printscript.source;

import java.io.InputStream;

final class SourceReaderFactoryJavaInterop {

    private SourceReaderFactoryJavaInterop() {
    }

    static void consumeFactoryFromJava(InputStream inputStream) {
        SourceReaderFactory.fromString("");
        SourceReaderFactory.fromInputStream(inputStream);
    }
}
