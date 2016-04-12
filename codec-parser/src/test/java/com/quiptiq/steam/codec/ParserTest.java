package com.quiptiq.steam.codec;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
        parser.addErrorListener(noneExpectedErrorListener);
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
        KeyValuesParser.KeypairContext keypairContext = parser.keypair();
        List nodes = keypairContext.KVTOKEN();
        assertEquals("Number of tokens", 2, nodes.size());
        assertEquals("Key", "\"Key\"", nodes.get(0).toString());
        assertEquals("Value", "\"Value\"", nodes.get(1).toString());
    }

    /**
     * Simple key/value pair can be extracted successfully
     * @throws IOException
     */
    @Test
    public void nestedKeyPair() throws IOException {
        prepareParser(asInputStream("\"Key\"\n{}"));
        KeyValuesParser.KeypairContext keypairContext = parser.keypair();
        List nodes = keypairContext.KVTOKEN();
        assertEquals("Number of tokens", 1, nodes.size());
        assertEquals("Key", "\"Key\"", nodes.get(0).toString());
        nodes = keypairContext.entry();
        assertEquals("Number of tokens", 0, nodes.size());
    }

    /**
     * When retrieved as an entry, a key/value pair can be extracted.
     * @throws IOException
     */
    @Test
    public void entryKeyPair() throws IOException {
        prepareParser(asInputStream("\"Key\" \"Value\""));
        KeyValuesParser.EntryContext entryContext = parser.entry();
        KeyValuesParser.KeypairContext keypairContext = entryContext.keypair();
        List nodes = keypairContext.KVTOKEN();
        assertEquals("Number of tokens", 2, nodes.size());
        assertEquals("Key", "\"Key\"", nodes.get(0).toString());
        assertEquals("Value", "\"Value\"", nodes.get(1).toString());
    }

    /**
     * Simplified appmanifest file can be walked through, generating the
     * expected entry/exit calls.
     * @throws IOException
     */
    @Test
    public void appManifest() throws IOException {
        File acfFile = loadResourceFile("/appmanifest.acf");
        prepareParser(new BufferedInputStream(new FileInputStream(acfFile)));
        parser.addErrorListener(noneExpectedErrorListener);

        ParseTreeWalker walker = new ParseTreeWalker();
        KeyValuesBaseListener testListener = mock(KeyValuesBaseListener.class);
        InOrder inOrder = inOrder(testListener);
        walker.walk(testListener, parser.keyvalues());

        inOrder.verify(testListener).enterKeyvalues(any()); // top level
        // initial comment is ignored
        // first keypair - AppState
        inOrder.verify(testListener).enterEntry(any());
        // AppState
        inOrder.verify(testListener).enterKeypair(argThat(new KeyMatcher("\"AppState\"")));
        inOrder.verify(testListener).enterEntry(any()); // child entry
        inOrder.verify(testListener).exitEntry(any());
        inOrder.verify(testListener).exitKeypair(any()); // exit AppState
        inOrder.verify(testListener).exitEntry(any()); // exit nested AppState

        // UserConfig
        inOrder.verify(testListener).enterEntry(any()); // third child entry, parent
        inOrder.verify(testListener).enterKeypair(argThat(new KeyMatcher("\"UserConfig\"")));
        // UserConfig children
        inOrder.verify(testListener).enterEntry(any()); // nested child entry
        inOrder.verify(testListener).exitKeypair(any());
        inOrder.verify(testListener).exitEntry(any()); // exit nest

        // MountedDepots
        inOrder.verify(testListener).enterEntry(any()); // final child entry
        inOrder.verify(testListener).enterKeypair(argThat(new KeyMatcher("\"MountedDepots\"")));
        // MountedDepots children
        inOrder.verify(testListener).enterEntry(any()); // first nested child entry
        inOrder.verify(testListener).exitEntry(any());
        inOrder.verify(testListener).enterEntry(any()); // second nested child entry
        inOrder.verify(testListener).exitEntry(any());
        inOrder.verify(testListener).exitKeypair(any()); // exit MountedDepots nesting
        inOrder.verify(testListener).exitEntry(any()); // exit MountedDepots
    }

    class KeyMatcher implements ArgumentMatcher<KeyValuesParser.KeypairContext> {
        private final String key;

        public KeyMatcher(String key) {
            this.key = key;
        }

        @Override
        public boolean matches(Object o) {
            if (!(o instanceof KeyValuesParser.KeypairContext)) {
                return false;
            }
            KeyValuesParser.KeypairContext keypairContext = (KeyValuesParser.KeypairContext) o;
            List kvToken = keypairContext.KVTOKEN();
            return kvToken.size() == 1 && key.equals(kvToken.get(0).toString());
        }
    }
    /**
     * Listener for errors that expects none
     */
    private ANTLRErrorListener noneExpectedErrorListener = new ANTLRErrorListener() {
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
