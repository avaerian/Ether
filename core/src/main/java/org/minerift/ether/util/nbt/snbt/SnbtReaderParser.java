package org.minerift.ether.util.nbt.snbt;

public class SnbtReaderParser { // follow the same principles as the original parser, just cleaned up

    private static final String TEST = "\"x:0\" :  {x: 0, y: 55, z: 0, Items: [{Slot: 0b, id: \"clock\", Count: 1b}, {Slot: 9b, id: \"written_book\", Count: 1b, tag: {pages: ['{\"text\":\"\\'twas brillig and the slithy toves\"}', '{\"text\":\"Did gyre and gimble in the wabe.\"}', '{\"text\":\"All mimsy were the borogoves,\"}', '{\"text\":\"And the mome raths outgrabe.\"}'], author: \"LewisCarroll\", title: \"Jabberwocky\"}}], id: \"enderchest and \"}";

    public static void main(String[] args) throws SnbtReadException {
        final String test2 = "{minecraft:[\"test1\", \"test2\", \"test3\", \"test4\"], test:}";
        TokenStream toks = new TokenStream(test2);
        CharSequence tok;
        while((tok = toks.nextToken()) != null) {
            System.out.println(tok);
        }
    }



    public static class Node {
        final CharSequence chars;
        final int nextStreamPos;
        final TokenStream stream;
        Node next;

        Node(TokenStream stream, CharSequence chars, int nextStreamPos) {
            this.chars = chars;
            this.nextStreamPos = nextStreamPos;
            this.stream = stream;
            this.next = null;
        }

        public Node next() throws SnbtReadException {
            if(next == null) {
                next = stream.nextTokenNode();
            }
            return next;
        }
    }

    // Format:  Ident:Value

    public static class TokenStream {
        final CharSequence chars;
        int pos;

        public TokenStream(CharSequence chars) {
            this.pos = 0;
            this.chars = chars;
        }

        public Node nextTokenNode() throws SnbtReadException {
            CharSequence tok = nextToken();
            return new Node(this, tok, pos);
        }

        // return null once no more tokens
        public CharSequence nextToken() throws SnbtReadException {
            int begin = pos;
            while(pos < chars.length()) {
                char c = chars.charAt(pos);
                switch (c) {
                    case ' ', '\n' -> { // ignore whitespaces (more exist; will do later)
                        pos++;
                        begin++;
                    }
                    case '\"', '\'' -> { // string values
                        if(pos - begin != 0) { // return ident);
                            return chars.subSequence(begin, pos);
                        }
                        final char quote = c; // can be inlined; provides clarity
                        char next;
                        while(pos + 1 < chars.length()
                                && (next = chars.charAt(++pos)) != quote) {
                            if(pos + 1 < chars.length() && next == '\\') {
                                pos++; // ignore the next character after backslash
                            }
                        }
                        CharSequence res = chars.subSequence(begin, pos + 1);
                        if(chars.charAt(pos) != quote) {
                            throw new SnbtReadException("Malformed token: " + res);
                        }
                        pos++;
                        return res;
                    }
                    case '{', '}', ':', ';', ',', '[', ']' -> {
                        if(pos - begin != 0) { // return ident
                            return chars.subSequence(begin, pos);
                        }
                        return chars.subSequence(begin, ++pos);
                    }
                    default -> { // for identifiers
                        pos++;
                    }
                }
            }
            return null;
        }
    }

}
