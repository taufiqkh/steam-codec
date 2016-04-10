package com.quiptiq.steam;

import com.quiptiq.steam.codec.KeyValuesLexer;
import com.quiptiq.steam.codec.KeyValuesParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reader for a given input stream
 */
public class SteamReader {
    private final KeyValuesParser parser;
    private final ParseTree tree;
    public SteamReader(InputStream stream) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(stream);

        KeyValuesLexer lexer = new KeyValuesLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        this.parser = new KeyValuesParser(tokens);

        this.tree = parser.keyvalues();
    }

    public String toString() {
        return tree.toStringTree(parser);
    }
}
