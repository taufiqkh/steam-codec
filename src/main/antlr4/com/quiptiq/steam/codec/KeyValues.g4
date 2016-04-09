/**
 * Limited subset of the full key values grammar, detailed here:
 * https://developer.valvesoftware.com/wiki/KeyValues
 * While it does not strictly conform, it should read most well-formed files.
 */
grammar KeyValues;

keyvalues: entry*
    ;

entry
    : keypair
    | COMMENT
    ;

keypair
    : KVTOKEN KVTOKEN
    | KVTOKEN '{' entry* '}'
    ;

KVTOKEN
    : STRING
    | ALPHANUMERIC+
    ;

WS: [ \n\r\t] -> channel(HIDDEN);

ALPHANUMERIC: [a-zA-Z0-9]
    ;

STRING: '"' (ESC | ~ ["])* '"'
    ;

ESC: '\\' ('n' | 't' | '\\' | '"')
    ;

COMMENT: '//' ~[\n]* ('\n' | EOF) -> channel(HIDDEN)
    ;