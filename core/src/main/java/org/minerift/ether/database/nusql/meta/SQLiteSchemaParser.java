package org.minerift.ether.database.nusql.meta;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.pair.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class SQLiteSchemaParser {

    public static final OnConflict DEFAULT_ON_CONFLICT = OnConflict.ABORT;

    @Debug
    public static void main(String[] args) {
        String query = """
                CREATE TABLE "users" (
                    "island_role"	varchar(255) NOT NULL,
                	"uuid"	varchar NOT NULL,
                	"island_id"	int,
                	"test"	INTEGER CHECK("test" >= 0) UNIQUE,
                	PRIMARY KEY("uuid"),
                	CONSTRAINT "island_role_enum" CHECK("island_role" IN ('VISITOR', 'MEMBER', 'OWNER')),
                	FOREIGN KEY("island_id") REFERENCES "islands"("island_id")
                );""";


        String query2 = """
                CREATE TABLE "islands" (
                	"island_id"	int NOT NULL,
                	"coords"	int8 NOT NULL,
                	"is_deleted"	boolean NOT NULL,
                	"members"	varchar(255) NOT NULL,
                	PRIMARY KEY("island_id")
                );""";

        String query3 = "CREATE TABLE t1(x INTEGER PRIMARY KEY);";

        SQLiteSchemaParser parser = new SQLiteSchemaParser(query);

        System.out.println(Arrays.toString(parser.result.tokens));
        System.out.println(Arrays.toString(parser.result.tokenIndexes));

        for(int i = 0; i < parser.result.tokenIndexes.length - 1; i++) {
            System.out.println(parser.result.sanitizedQuery.substring(parser.result.tokenIndexes[parser.idx], parser.result.tokenIndexes[parser.idx + 1]));
            parser.idx++;
        }
        parser.idx = 0;

        try {
            parser.parseTable();
        } finally {
            System.out.println("Parser state: " + parser);
        }
    }

    public static class TokensResult {
        public String sanitizedQuery;
        public String[] tokens;
        public int[] tokenIndexes; // where each token begins in the sanitized query
    }

    public static TokensResult tokenizeSchema(String schema) {
        System.out.println(schema);
        schema = schema.replaceAll("\n", "").replaceAll("\t", " ");
        System.out.println(schema);

        int idx = 0;
        int tokenStart = 0;
        List<String> tokens = new ArrayList<>();
        IntList tokenIndexes = new IntArrayList();
        StringBuilder tok = new StringBuilder();
        while(true) {
            if(idx == schema.length()) {
                tokens.add(tok.toString());
                tokenIndexes.add(tokenStart);
                break;
            }

            char c = schema.charAt(idx);
            switch(c) {
                case ' ' -> {
                    if(!tok.isEmpty()) {
                        tokens.add(tok.toString());
                        tokenIndexes.add(tokenStart);

                        tok = new StringBuilder();
                    }
                    tokenStart = idx + 1;
                }
                case ',' -> {
                    if(!tok.isEmpty()) {
                        tokens.add(tok.toString());
                        tokenIndexes.add(tokenStart);
                    }
                    tokens.add(",");
                    tokenIndexes.add(idx);
                    tok = new StringBuilder();
                    tokenStart = idx + 1;
                }
                case '(', ')' -> {
                    if(!tok.isEmpty()) {
                        tokens.add(tok.toString());
                        tokenIndexes.add(tokenStart);
                        tok = new StringBuilder();
                    }
                    tokens.add(String.valueOf(c));
                    tokenIndexes.add(idx);
                    tokenStart = idx + 1;
                }
                default -> tok.append(c);
            }
            idx++;
        }
        tokenIndexes.add(schema.length());

        TokensResult result = new TokensResult();
        result.sanitizedQuery = schema;
        result.tokens = tokens.toArray(String[]::new);
        result.tokenIndexes = tokenIndexes.toIntArray();
        return result;
    }

    private TokensResult result;
    private final String query;
    private int idx;

    private Table tableDef;
    private String currentConstraintName;

    public SQLiteSchemaParser(String query) {
        this.result = tokenizeSchema(query);
        this.query = result.sanitizedQuery;
        this.idx = 0;
        this.tableDef = new Table();
    }

    /**
     *
     * CREATE TABLE "users" (
     *  "uuid"	varchar NOT NULL,
     * 	"island_id"	int,
     * 	"island_role"	varchar(255),
     * 	"test"	INTEGER CHECK("test" >= 0) UNIQUE,
     * 	PRIMARY KEY("uuid"),
     * 	CONSTRAINT "island_role_enum" CHECK("island_role" IN ('VISITOR', 'MEMBER', 'OWNER')),
     * 	FOREIGN KEY("island_id") REFERENCES "islands"("island_id")
     * );
     *
     */

    public List<String> getTokensBetweenParens() {
        expect(Token.OPEN_PAREN);

        // TODO: take scope into account for properness
        List<String> strs = new ArrayList<>();
        while(!test(Token.CLOSE_PAREN)) {
            strs.add(result.tokens[idx++]);
        }

        expect(Token.CLOSE_PAREN);
        return strs;
    }

    public ColumnInfo parseColumn() {
        var col = new ColumnInfo();

        col.name = expectIdent();
        col.type = expectIdent(); // TODO: allow for reading multi-word data types

        if(test(Token.OPEN_PAREN)) {
            List<String> tokensInParens = getTokensBetweenParens();
            String content = String.join(" ", tokensInParens);
            col.type += "(" + content + ")";
        }

        // Column constraints
        while(!test(Token.COMMA) && !test(Token.CLOSE_PAREN)) {
            switch (next()) {
                case NOT -> {
                    expect(Token.NULL);
                    col.notNull = true;
                }
                case UNIQUE -> {
                    col.unique = true;
                }
                case PRIMARY -> {
                    expect(Token.KEY);
                    tableDef.primaryKey = col.name;

                    if(nextIf(Token.ASC)) {
                        col.order = "ASC";
                    } else if(nextIf(Token.DESC)) {
                        col.order = "DESC";
                    }

                    col.onConflict = onConflictClause();

                    if(nextIf(Token.AUTOINCREMENT)) {
                        col.autoincrement = true;
                    }
                }
                case CHECK -> {
                    // TODO: improve
                    List<String> tokensInParens = getTokensBetweenParens();
                    String content = String.join(" ", tokensInParens);
                    col.checks.add(content);
                }


                // ignored; included for proper parsing
                case GENERATED -> {
                    expect(Token.ALWAYS);
                }
                case AS -> {
                    expect(Token.OPEN_PAREN);

                    int scope = 0;
                    while(scope >= 0) {
                        switch (next()) {
                            case OPEN_PAREN -> scope++;
                            case CLOSE_PAREN -> scope--;
                            // TODO: handle other tokens appropriately
                        }
                    }

                    if(nextIf(Token.STORED)) {

                    } else if(nextIf(Token.VIRTUAL)) {

                    }
                }
            }
        }

        System.out.println(col);

        return col;
    }

    // Returns the current token and moves to the next
    public Token next() {
        return Token.from(result.tokens[idx++]);
    }

    public void parseTable() {
        expect(Token.CREATE);
        if(nextIf(Token.TEMPORARY)) {
            tableDef.isTemp = true;
        }
        expect(Token.TABLE);
        tableDef.tableName = expectIdent();

        expect(Token.OPEN_PAREN);

        while(!test(Token.CLOSE_PAREN)) {
            System.out.println(currentToken());
            switch (currentToken()) {
                case IDENT -> {
                    var col = parseColumn();
                    tableDef.columns.put(col.name, col);
                }
                case CONSTRAINT, PRIMARY, FOREIGN, UNIQUE, CHECK -> {
                    parseTableConstraint();
                }
                default -> throw new IllegalStateException("Unexpected token: " + currentToken());
            }
            nextIf(Token.COMMA);
        }

        expect(Token.CLOSE_PAREN);

        // TODO: create isAtEnd() and handle optional semicolon
        // Table options
        while(currentToken() != Token.SEMICOLON) {
            switch (next()) {
               case WITHOUT -> {
                   expect(Token.ROWID);
               }
               case STRICT -> {

               }
               default -> throw new IllegalStateException("Unexpected token " + currentToken());
            }
            nextIf(Token.COMMA);
        }

        expect(Token.SEMICOLON);
    }

    public OnConflict onConflictClause() {
        if(nextIf(Token.ON)) {
            expect(Token.CONFLICT);
            Token current = next();
            OnConflict conflict = switch (current) {
                case ROLLBACK -> OnConflict.ROLLBACK;
                case ABORT -> OnConflict.ABORT;
                case FAIL -> OnConflict.FAIL;
                case IGNORE -> OnConflict.IGNORE;
                case REPLACE -> OnConflict.REPLACE;
                default -> throw new IllegalStateException("Unexpected token " + current);
            };
            return conflict;
        }
        return OnConflict.NO_CLAUSE;
    }

    public void parseTableConstraint() {
        if(nextIf(Token.CONSTRAINT)) {
            currentConstraintName = expectIdent();
        }

        switch(next()) {
            case PRIMARY -> {
                expect(Token.KEY);
                expect(Token.OPEN_PAREN);
                tableDef.primaryKey = expectIdent();
                expect(Token.CLOSE_PAREN);
            }

            case FOREIGN -> {
                expect(Token.KEY);
                expect(Token.OPEN_PAREN);

                String childField = expectIdent();

                expect(Token.CLOSE_PAREN);

                // foreign key clause
                expect(Token.REFERENCES);

                String parentModel = expectIdent();

                expect(Token.OPEN_PAREN);
                String parentCol = expectIdent();
                expect(Token.CLOSE_PAREN);

                tableDef.childCol2ParentModel.put(childField, new Pair<>(parentModel, parentCol));
            }

            case CHECK -> {
                expect(Token.OPEN_PAREN);

                int start = result.tokenIndexes[idx];
                StringBuilder predicate = new StringBuilder();
                int scope = 0;
                while(scope >= 0) {
                    switch(currentToken()) {
                        case OPEN_PAREN -> scope++;
                        case CLOSE_PAREN -> scope--;
                        //default -> predicate.append(result.tokens[idx]).append(' ');
                    }
                    next();
                }
                //if(!predicate.isEmpty()) predicate.deleteCharAt(predicate.length() - 1);

                System.out.println("Predicate: " + query.substring(start, result.tokenIndexes[idx] - 1));
                //System.out.println(query.substring(result.tokenIndexes[idx], result.tokenIndexes[idx + 1]));

            }

            case UNIQUE -> {
                expect(Token.OPEN_PAREN);

                List<String> cols = new ArrayList<>(2);
                do {
                    cols.add(expectIdent());
                } while(nextIf(Token.COMMA));

                expect(Token.CLOSE_PAREN);
            }
        }
    }

    public String expectIdent() {
        Token current = currentToken();
        if(!test(Token.IDENT)) {
            throw new IllegalStateException("Expected " + Token.IDENT + ", received " + current + " instead");
        }
        return result.tokens[idx++];
    }

    // Expects the current token to be the token passed and progresses forward to the next token
    public void expect(Token tok) {
        Token current = currentToken();
        if(!test(tok)) {
            throw new IllegalStateException("Expected " + tok + ", received " + current + " instead");
        }
        idx++;
    }

    public boolean nextIf(Token tok) {
        if(test(tok)) {
            idx++;
            return true;
        }
        return false;
    }

    // Tests the current token; does NOT progress forward
    public boolean test(Token tok) {
        Token current = currentToken();
        return current == tok;
    }

    public Token currentToken() {
        return Token.from(result.tokens[idx]);
    }

    public String getSurroundingTokens(int idx, int count) {
        int half = (count >> 1);
        int start = Math.max(idx - half, 0);
        int end = Math.min(idx + half, result.tokens.length);
        return Arrays.stream(result.tokens, start, end)
                .collect(Collectors.joining(" "));
    }

    @Override
    public String toString() {
        return "SQliteSchemaParserV2{" +
                "current=" + (idx >= result.tokens.length ? null : result.tokens[idx]) +
                ", surround=" + (idx >= result.tokens.length ? null : getSurroundingTokens(idx, 10)) +
                //"tokens=" + Arrays.toString(tokens) +
                //", query='" + query + '\'' +
                ", idx=" + idx +
                ", tableDef=" + tableDef +
                '}';
    }
}
