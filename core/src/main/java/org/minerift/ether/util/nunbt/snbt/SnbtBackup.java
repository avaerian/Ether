package org.minerift.ether.util.nunbt.snbt;

import com.google.common.base.Preconditions;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.util.nunbt.tags.Tag;
import org.minerift.ether.util.nunbt.tags.TagType;
import org.minerift.ether.util.nunbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nunbt.tags.container.AbstractContainerTag;
import org.minerift.ether.util.nunbt.tags.container.CompoundTag;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
@Deprecated
public class SnbtBackup {

    public static final Pattern KEY_ACCEPTED_CHARS = Pattern.compile("[a-zA-Z0-9_\\-.+]");

    // Names are optional (only for outside)
    // Values are always expected
    // When first reading, check for comma (as in this is next element) or name. Read value. If value is a compound, expect name and value. If list, expect values.

    @Debug
    public static void main(String[] args) {
        //final String test = "{name1:123,name2:\"sometext1\",name3:{subname1:456,subname2:\"sometext2\"}}";
        final String test = "\"x:0\" :  {x: 0, y: 55, z: 0, Items: [{Slot: 0b, id: \"clock\", Count: 1b}, {Slot: 9b, id: \"written_book\", Count: 1b, tag: {pages: ['{\"text\":\"\\'twas brillig and the slithy toves\"}', '{\"text\":\"Did gyre and gimble in the wabe.\"}', '{\"text\":\"All mimsy were the borogoves,\"}', '{\"text\":\"And the mome raths outgrabe.\"}'], author: \"LewisCarroll\", title: \"Jabberwocky\"}}], id: \"enderchest and \"}";
        //final String test = "Id: [I;0,0,0,1]";
        //final String test = "{test_tag:\"1\\\"2\\\\\\\"3'4\\\\\"}";
        var tokens = new TokenStream(test);
        System.out.println(tokens.chars);
        while(tokens.hasNext()) {
            String tok = tokens.readNextToken();
            System.out.println(tok + " : " + Parser.getTagType(tokens, tok));
            //System.out.println(tok.translateEscapes());
        }

        Tag<?> tag = readTag(test);
    }

    public static class Parser {

        public static final Predicate<String> BYTE_VALUE    = Pattern.compile("(\\+|-)?\\d+(b|B)").asMatchPredicate();
        public static final Predicate<String> SHORT_VALUE   = Pattern.compile("(\\+|-)?\\d+(s|S)").asMatchPredicate();
        public static final Predicate<String> INT_VALUE     = Pattern.compile("(\\+|-)?\\d+").asMatchPredicate();
        public static final Predicate<String> LONG_VALUE    = Pattern.compile("(\\+|-)?\\d+(l|L)").asMatchPredicate();
        public static final Predicate<String> FLOAT_VALUE   = Pattern.compile("(\\+|-)?\\d+\\.\\d+(f|F)").asMatchPredicate();
        public static final Predicate<String> DOUBLE_VALUE  = Pattern.compile("(\\+|-)?\\d+\\.\\d+(d|D)?").asMatchPredicate();
        public static final Predicate<String> STRING_VALUE  = Pattern.compile("^(\"|').*\\1$").asMatchPredicate();




        private final TokenStream stream;


        public Parser(TokenStream stream) {
            this.stream = stream;
        }

        public Parser(String snbt) {
            this(new TokenStream(snbt));
        }

        public void expect(String tok) {
            stream.readNextToken();
        }


        public static TagType getTagType(TokenStream toks, String tok) {
            return switch (tok) {
                case "{" -> TagType.COMPOUND;
                case "[" -> {
                    TagType type = switch (toks.peekNextToken()) {
                        case "B" -> TagType.BYTE_ARRAY;
                        case "I" -> TagType.INT_ARRAY;
                        case "L" -> TagType.LONG_ARRAY;
                        default -> TagType.LIST;
                    };
                    if(type != TagType.LIST) { // if array type
                        toks.readNextToken(); // skip array type
                        toks.readNextToken(); // skip ";"
                    }
                    yield type;
                }
                default -> {
                    if(tok.equalsIgnoreCase("true") || tok.equalsIgnoreCase("false")) {
                        yield TagType.BYTE;
                    }

                    TagType type;
                    if(BYTE_VALUE.test(tok)) {
                        type = TagType.BYTE;
                    } else if(SHORT_VALUE.test(tok)) {
                        type = TagType.SHORT;
                    } else if(INT_VALUE.test(tok)) {
                        type = TagType.INT;
                    } else if(LONG_VALUE.test(tok)) {
                        type = TagType.LONG;
                    } else if(FLOAT_VALUE.test(tok)) {
                        type = TagType.FLOAT;
                    } else if(DOUBLE_VALUE.test(tok)) {
                        type = TagType.DOUBLE;
                    } else if(STRING_VALUE.test(tok)) {
                        type = TagType.STRING;
                    } else {
                        type = null;
                        //throw new IllegalArgumentException("'" + tok + "' is not a valid tag type!");
                    }

                    yield type;
                }
            };
        }

        // TODO
        public ByteArrayTag readByteArray() {
            throw new UnreachableException();
        }



    }

    public static Tag<?> readTag(String snbt) {

        // Format: Name:Value

        TokenStream tokens = new TokenStream(snbt);
        Stack<AbstractContainerTag<?>> containerStack = new Stack<>();
        Queue<String> tokQueue = new ArrayDeque<>();
        String name = "";

        while(tokens.hasNext()) {
            final String token = tokens.readNextToken();
            switch (token) {
                case "{" -> {
                    containerStack.push(new CompoundTag(name));
                }

                case "}" -> {
                    containerStack.pop();
                }

                case "[" -> {
                    TagType type = Parser.getTagType(tokens, token);
                    // Goal for this parser/token reader: create tags and assign values
                    /*switch (type) { // FIXME: work on this
                        case BYTE_ARRAY -> containerStack.push(new ByteArrayTag(name));
                    }*/
                }

                case ":" -> {
                    String tok = tokQueue.peek();
                    if(tok == null) {
                        throw new IllegalStateException("Colon found, but no preceding name available");
                    }
                    name = tok;
                }

                default -> {
                    tokQueue.add(token);
                }

                /*
                 *  { -> open CompoundTag
                 *  } -> close CompoundTag
                 *  [ -> open List OR Array
                 *  ] -> close List OR Array
                 *  : -> expect value
                 *  , -> next tag
                 *  default -> name/ident; key or value for tag
                 *
                 *
                 *  once value is reached, create tag with name and push to stack
                 *
                 *
                 */
            }
        }




        return null;
    }

    public static class TokenStream {

        private final CharSequence chars;
        private int idx;

        public TokenStream(CharSequence snbt) {
            this.chars = snbt;
            this.idx = 0;
        }

        public String peekNextToken() {
            int init = idx;
            String tok = readNextToken();
            idx = init;
            return tok;
        }

        /**
         * Read the next token from the SNBT string.
         * <p><br>
         * NOTE: returns tokens unescaped; this will return tokens copied exactly as
         * written in the SNBT string, so unescaping these strings are delegated to
         * the user.
         * </p>
         *
         * @return next token
         */
        public String readNextToken() {
            Preconditions.checkState(idx < chars.length(), "End of stream"); // TODO: return null instead of throw exception?
            int begin = idx;
            boolean isQuoted = isQuoted(chars.charAt(idx));
            do {
                char c = chars.charAt(idx);
                switch(c) {
                    case ' ' -> {
                        idx++;
                        if(!isQuoted) {
                            begin++;
                            isQuoted = isQuoted(chars.charAt(idx));
                        }
                    }
                    case '"', '\'' -> {
                        final char quote = c; // can be inlined; here for readability
                        idx++;
                        char next;
                        while((next = chars.charAt(idx)) != quote) {
                            if(next == '\\') {
                                switch (chars.charAt(++idx)) {
                                    case '\'', '\"', '\\' -> {
                                        idx++;
                                        continue;
                                    }
                                }
                            }
                            idx++;
                        }
                        return chars.subSequence(begin, ++idx).toString();
                    }
                    case '{', '}', ':', ',', ';', '[', ']' -> {
                        if(isQuoted) {
                            idx++;
                            continue;
                        }
                        if(idx - begin != 0) { // if not empty
                            return chars.subSequence(begin, idx).toString();
                        }
                        idx++;
                        return String.valueOf(c);
                    }
                    default -> {
                        idx++;
                    }
                }
            } while(idx < chars.length());
            throw new UnreachableException("unexpected"); // FIXME: change to TokenReadException (this is reachable if str is malformed)
        }

        private boolean isQuoted(char c) {
            return c == '\'' || c == '"';
        }

        public boolean hasNext() {
            return idx < chars.length();
        }

    }

}
