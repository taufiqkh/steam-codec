package com.quiptiq.steam;

import com.quiptiq.steam.codec.KeyValuesBaseListener;
import com.quiptiq.steam.codec.KeyValuesParser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class KeyValueParseListener extends KeyValuesBaseListener {
    private Map<String, Object> tree = new HashMap<>();
    private ParseState state = ParseState.KEYVALUES;
    private String errorMessage;
    private Node root;
    private Node current = null;

    @Override
    public void enterKeyvalues(@NotNull KeyValuesParser.KeyvaluesContext ctx) {
        if (state != ParseState.KEYVALUES && state != ParseState.ERROR) {
            state = ParseState.ERROR;
            errorMessage = "Entered keyValues state after progressing through parse tree";
        }
    }

    @Override
    public void enterKeypair(@NotNull KeyValuesParser.KeypairContext ctx) {
        if (state == ParseState.ERROR) {
            return;
        }
        List<TerminalNode> segments = ctx.KVTOKEN();
        Node node;
        if (segments.size() == 1) {
            node = new Node(current, segments.get(0).toString(), new ArrayList<>());
        } else if (segments.size() == 2) {
            node = new Node(current, segments.get(0).toString(), segments.get(1).toString());
        } else {
            state = ParseState.ERROR;
            errorMessage = "Encountered keypair with unexpected number of elements: " + segments.size();
            return;
        }
        if (state == ParseState.KEYVALUES) {
            root = node;
        }
        state = ParseState.KEYPAIR;
    }

    @Override
    public void exitKeypair(@NotNull KeyValuesParser.KeypairContext ctx) {
        current = current.getParent();
    }

    private enum ParseState
    {
        KEYVALUES,
        KEYPAIR,
        ERROR;
    }
}
