package org.minerift.ether.database.sql.meta;

import java.util.HashMap;
import java.util.Map;

public enum Token {

    CREATE,
    TEMPORARY("TEMP", "TEMPORARY"),
    TABLE,
    IF, EXISTS,
    IDENT,
    NOT,
    NULL,
    REFERENCES,
    CONSTRAINT, PRIMARY, FOREIGN, KEY, UNIQUE, CHECK,
    IN,
    AS,


    WITHOUT, ROWID,
    STRICT,
    ON,
    DELETE,
    UPDATE,
    SET,
    DEFAULT,
    CASCADE,
    RESTRICT,
    NO,
    ACTION,
    MATCH,
    DEFERRABLE,
    INITIALLY, DEFERRED, IMMEDIATE,
    COLLATE,
    ASC, DESC,
    AUTOINCREMENT,
    CONFLICT, ROLLBACK, ABORT, FAIL, IGNORE, REPLACE,
    GENERATED,
    ALWAYS,
    STORED, VIRTUAL,

    COMMA(","),
    SEMICOLON(";"),
    OPEN_PAREN("("),
    CLOSE_PAREN(")"),

    // Boolean operators
    AND, OR,
    CMP_LESS_THAN("<"),
    CMP_LESS_THAN_OR_EQUAL("<="),
    CMP_EQUAL("="),
    CMP_GREATER_THAN(">"),
    CMP_GREATER_THAN_OR_EQUAL(">="),


    ;

    public static final Map<String, Token> NAMES_TO_TOKEN_LOOKUP;

    static {
        Token[] tokens = values();
        NAMES_TO_TOKEN_LOOKUP = new HashMap<>(tokens.length);
        for (Token tok : tokens) {
            for (String name : tok.names) {
                NAMES_TO_TOKEN_LOOKUP.put(name, tok);
            }
        }
        NAMES_TO_TOKEN_LOOKUP.remove(Token.IDENT.name());
    }

    private final String[] names;

    Token(String... names) {
        if (names.length == 0) {
            names = new String[1];
            names[0] = name();
        }
        this.names = names;
    }

    public static Token from(String str) {
        return NAMES_TO_TOKEN_LOOKUP.getOrDefault(str, Token.IDENT);
    }

}
