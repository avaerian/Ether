package org.minerift.ether.util.yaml.graph;

import org.minerift.ether.debug.Debug;
import org.minerift.ether.debug.NotWorking;

import java.util.function.Supplier;

@NotWorking
public class Parser {

    private static final String TEST_TEXT = """
            server_config:
              port_mapping:
                # Expose only ssh and http to the public internet.
                - 22:22
                - 80:80
                - 443:443
            
              serve:
                - /robots.txt
                - /favicon.ico
                - *.html
                - *.png
                - !.git  # Do not expose our Git repository to the entire world.
            
              geoblock_regions:
                # The legal team has not approved distribution in the Nordics yet.
                - dk
                - fi
                - is
                - no
                - se
            
              flush_cache:
                on: [push, memory_pressure]
                priority: background
            
              allow_postgres_versions:
                - 9.5.25
                - 9.6.24
                - 10.23
                - 12.13
            """;

    private int i;
    private CharSequence chars;
    public Parser(CharSequence chars) {
        this.chars = chars;
    }

    public Node parse() {
        if(test(" "))
            throw new IllegalStateException("Unexpected whitespace at beginning");
        String name;
        if((name = expectName()) == null)
            throw new IllegalStateException("Expected name");

        expect("\n");

        int whitespaces = 0;
        int expectedWhitespaces = 2;

        for(i = 0; i < chars.length(); i++) {
            final char c = chars.charAt(i);
            switch (c) {
                case ' ' -> {
                    if(whitespaces++ > expectedWhitespaces) {
                        throw new IllegalStateException(
                                "Expected " + expectedWhitespaces + " whitespaces, but got " + whitespaces);
                    }
                }
                case '\n' -> {
                    // start new node entry (unknown to us at this time); for block mode
                    if(nextIf("\n")) {
                        expectedWhitespaces -= 2;
                    } else {
                        expectedWhitespaces += 2;
                    }
                }
                case '-' -> {
                    expect(" ", () -> new IllegalStateException("Expected a whitespace after '-'"));
                }
                case '{' -> {
                    // expect a new node
                }
                case '#' -> {
                    // expect a new comment
                    nextIf(" ");
                    char c1;
                    StringBuilder comment = new StringBuilder();
                    while((c1 = chars.charAt(++i)) != '\n') {
                        comment.append(c1);
                    }
                }
                default -> {
                    name = expectName();
                    expect(":");
                }
            }
        }

        return null;
    }

    public String expectName() {
        StringBuilder str = new StringBuilder();
        char cs;
        while(i < chars.length() && (cs = chars.charAt(i++)) != ':') {
            str.append(cs);
        }
        return str.isEmpty() ? null : str.toString();
    }

    public boolean testName() {
        return expectName() == null;
    }

    public boolean expect(String expect) {
        return chars.subSequence(i, i += expect.length()).equals(expect);
    }

    public <E extends Exception> void expect(String expect, Supplier<E> exSupplier) throws E {
        if(!chars.subSequence(i, i += expect.length()).equals(expect)) {
            throw exSupplier.get();
        }
    }

    public boolean test(String test) {
        return chars.subSequence(i, i + test.length()).equals(test);
    }

    public boolean nextIf(String next) {
        if(test(next)) {
            i += next.length();
            return true;
        }
        return false;
    }

    @Debug
    public static void main(String[] args) {
        Parser parser = new Parser(TEST_TEXT);
        try {
            parser.parse();
        } finally {
            System.out.println(parser.toString());
            System.out.println(parser.chars.subSequence(Math.max(parser.i - 10, 0), Math.min(parser.i + 10, parser.chars.length())));
        }
    }

    public enum Style {
        BLOCK,
        FLOW,
        AUTO
    }

    @Override
    public String toString() {
        return "Parser{" +
                "i=" + i +
                ", chars=" + chars +
                '}';
    }
}
