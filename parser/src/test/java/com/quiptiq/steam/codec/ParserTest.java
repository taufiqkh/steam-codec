package com.quiptiq.steam.codec;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.List;

/**
 * Tests basic parser functionality
 */
public class ParserTest {
    private KeyValuesParser parser;

    private File loadResourceFile(String path) {
        return new File(this.getClass().getResource(path).getFile());
    }

    private void prepareParser(InputStream inputStream) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(inputStream);

        KeyValuesLexer lexer = new KeyValuesLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        parser = new KeyValuesParser(tokens);
    }


    private InputStream asInputStream(String string) {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void emptyIsAccepted() throws IOException {
        prepareParser(asInputStream(""));
    }

    /**
     * A simple key/value pair should be parsed and returned as such.
     * @throws IOException
     */
    @Test
    public void simpleKeyPair() throws IOException {
        prepareParser(asInputStream("\"Key\" \"Value\""));
        parser.addErrorListener(errorListener);
        KeyValuesParser.KeypairContext keypairContext = parser.keypair();
        List nodes = keypairContext.KVTOKEN();
        assertEquals("Number of tokens", 2, nodes.size());
        assertEquals("Key", "\"Key\"", nodes.get(0).toString());
        assertEquals("Value", "\"Value\"", nodes.get(1).toString());
    }

    @Test
    public void nestedKeyPair() throws IOException {
        prepareParser(asInputStream("\"Key\"\n{}"));
        parser.addErrorListener(errorListener);
        KeyValuesParser.KeypairContext keypairContext = parser.keypair();
        List nodes = keypairContext.KVTOKEN();
        assertEquals("Number of tokens", 1, nodes.size());
        assertEquals("Key", "\"Key\"", nodes.get(0).toString());
        nodes = keypairContext.entry();
        assertEquals("Number of tokens", 0, nodes.size());
    }

    @Test
    public void appManifest() throws IOException {
        File acfFile = loadResourceFile("/appmanifest.acf");
        prepareParser(new BufferedInputStream(new FileInputStream(acfFile)));
        parser.addErrorListener(errorListener);
        KeyValuesParser.KeyvaluesContext keyvaluesContext = parser.keyvalues();
    }

    private ANTLRErrorListener errorListener = new ANTLRErrorListener() {
        @Override
        public void syntaxError(@NotNull Recognizer<?, ?> recognizer, @Nullable Object o, int i, int i1, @NotNull String s, @Nullable RecognitionException e) {
            assertTrue("Unexpected syntax error", false);
        }

        @Override
        public void reportAmbiguity(@NotNull Parser parser, @NotNull DFA dfa, int i, int i1, boolean b, @Nullable BitSet bitSet, @NotNull ATNConfigSet atnConfigSet) {
            assertTrue("Unexpected ambiguity", false);
        }

        @Override
        public void reportAttemptingFullContext(@NotNull Parser parser, @NotNull DFA dfa, int i, int i1, @Nullable BitSet bitSet, @NotNull ATNConfigSet atnConfigSet) {
            // TODO unsure what this does
            assertTrue("Full context found", false);
        }

        @Override
        public void reportContextSensitivity(@NotNull Parser parser, @NotNull DFA dfa, int i, int i1, int i2, @NotNull ATNConfigSet atnConfigSet) {
            // TODO unsure what this does
            assertTrue("Context sensitivity found", false);
        }
    };
}
