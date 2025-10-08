package org.minerift.ether.util.nbt.snbt;

import com.google.common.base.Preconditions;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.TagType;
import org.minerift.ether.util.nbt.tags.TagTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Snbt {

    public static final Pattern KEY_ACCEPTED_CHARS = Pattern.compile("[a-zA-Z0-9_\\-.+]");

    // Names are optional (only for outside)
    // Values are always expected
    // When first reading, check for comma (as in this is next element) or name. Read value. If value is a compound, expect name and value. If list, expect values.

    @Debug
    public static void main(String[] args) throws UnexpectedTokenException {
        //final String test = "{name1:123,name2:\"sometext1\",name3:{subname1:456,subname2:\"sometext2\"}}";
        final String test = "\"x:0\" :  {x: 0, y: 55, z: 0, Items: [{Slot: 0b, id: \"clock\", Count: 1b}, {Slot: 9b, id: \"written_book\", Count: 1b, tag: {pages: ['{\"text\":\"\\'twas brillig and the slithy toves\"}', '{\"text\":\"Did gyre and gimble in the wabe.\"}', '{\"text\":\"All mimsy were the borogoves,\"}', '{\"text\":\"And the mome raths outgrabe.\"}'], author: \"LewisCarroll\", title: \"Jabberwocky\"}}], id: \"enderchest and \"}";
        //final String test = "Id: [I;0,0,0,1]";
        //final String test = "{test_tag:\"1\\\"2\\\\\\\"3'4\\\\\"}";
        var tokens = new TokenStream(test);
        System.out.println(tokens.chars);
        /*while(tokens.hasNext()) {
            Token tok = tokens.readNextToken();
            System.out.println(tok + " : " + Parser.getTagType(tokens, tok));
            //System.out.println(tok.translateEscapes());
        }*/

        System.out.println(Byte.valueOf("1"));
        System.out.println(Short.valueOf("1"));
        System.out.println(Integer.valueOf("1"));
        System.out.println(Long.valueOf("1"));
        System.out.println(Float.valueOf("+1.0f"));
        System.out.println(Double.valueOf("+1.0d"));

        System.out.println(Parser.DOUBLE_VALUE.matcher("-555.0d").matches());

        Tag tag = readTag(test);
        System.out.println(test);
        System.out.println(tag);

        System.out.println("Snbt: " + Snbt.writeTag(tag));
    }

    // FIXME: Snbt class needs to be cleaned up and polished

    public static class Parser {

        // review re-adding the optional '+' for testing positive numbers
        public static final Pattern BYTE_VALUE    = Pattern.compile("(-?)(\\d+)(b|B)");
        public static final Pattern SHORT_VALUE   = Pattern.compile("(-?)(\\d+)(s|S)");
        public static final Pattern INT_VALUE     = Pattern.compile("(-?)(\\d+)");
        public static final Pattern LONG_VALUE    = Pattern.compile("(-?)(\\d+)(l|L)");
        public static final Pattern FLOAT_VALUE   = Pattern.compile("(\\+|-)?(\\d+\\.\\d+)(f|F)");
        public static final Pattern DOUBLE_VALUE  = Pattern.compile("(\\+|-)?(\\d+\\.\\d+)(d|D)?");
        public static final Pattern STRING_VALUE  = Pattern.compile("^(\"|').*\\1$");


        private final TokenStream stream;


        public Parser(TokenStream stream) {
            this.stream = stream;
        }

        public Parser(String snbt) {
            this(new TokenStream(snbt));
        }

        public TokenStream getTokens() {
            return stream;
        }

        public void expect(String expected) throws UnexpectedTokenException {
            Token tok = stream.readNextToken();
            if(!tok.matches(expected)) {
                throw new UnexpectedTokenException("Expected token '" + expected + "', found '" + tok + "'");
            }
        }

        public String expectName() throws UnexpectedTokenException {
            Token tok = stream.readNextToken();
            if(!tok.next().matches(":")) {
                throw new UnexpectedTokenException("Expected a name, found '" + tok + "'");
            }

            // TODO: test token if it fits regex

            return tok.strTok;
        }

        private String expectScalar(Token tok, String type, Pattern regex) throws UnexpectedTokenException {
            Matcher matcher = regex.matcher(tok.strTok);
            if(!matcher.matches()) {
                throw new UnexpectedTokenException("Expected " + type + ", found " + tok);
            }
            return matcher.replaceFirst("$1$2");
        }

        public byte expectByte(Token tok) throws UnexpectedTokenException {
            String scalar = expectScalar(tok, "byte", BYTE_VALUE);
            return Byte.parseByte(scalar);
        }

        public byte expectByte() throws UnexpectedTokenException {
            return expectByte(stream.readNextToken());
        }

        public short expectShort(Token tok) throws UnexpectedTokenException {
            String scalar = expectScalar(tok, "short", SHORT_VALUE);
            return Short.parseShort(scalar);
        }

        public short expectShort() throws UnexpectedTokenException {
            return expectShort(stream.readNextToken());
        }

        public int expectInt(Token tok) throws UnexpectedTokenException {
            String scalar = expectScalar(tok, "int", INT_VALUE);
            return Integer.parseInt(scalar);
        }

        public int expectInt() throws UnexpectedTokenException {
            return expectInt(stream.readNextToken());
        }

        public long expectLong(Token tok) throws UnexpectedTokenException {
            String scalar = expectScalar(tok, "long", LONG_VALUE);
            return Long.parseLong(scalar);
        }

        public long expectLong() throws UnexpectedTokenException {
            return expectLong(stream.readNextToken());
        }

        public float expectFloat(Token tok) throws UnexpectedTokenException {
            String scalar = expectScalar(tok, "float", FLOAT_VALUE);
            return Float.parseFloat(scalar);
        }

        public float expectFloat() throws UnexpectedTokenException {
            return expectFloat(stream.readNextToken());
        }

        public double expectDouble(Token tok) throws UnexpectedTokenException {
            String scalar = expectScalar(tok, "double", DOUBLE_VALUE);
            return Double.parseDouble(scalar);
        }

        public double expectDouble() throws UnexpectedTokenException {
            return expectDouble(stream.readNextToken());
        }

        public String expectUTF8(Token tok) throws UnexpectedTokenException {
            Matcher matcher = STRING_VALUE.matcher(tok.strTok);
            if(!matcher.matches()) {
                throw new UnexpectedTokenException("Expected UT8 string token, found: " + tok);
            }
            return tok.strTok.substring(1, tok.strTok.length() - 1);
        }

        public String expectUTF8() throws UnexpectedTokenException {
            return expectUTF8(stream.readNextToken());
        }


        public boolean isName(Token tok) {
            return tok.next().matches(":"); // TODO: add condition for checking if it fits regex
        }


        public Token next() {
            return stream.readNextToken();
        }

        public Token peek() {
            return stream.peekNextToken();
        }

        public boolean nextIf(String test) {
            Token tok = stream.readToken(stream.pos);
            if(tok.matches(test)) {
                stream.pos = tok.nextStreamPos;
                return true;
            }
            return false;
        }

        public TagTypeParserResult getTagType(Token tok) {
            return getTagType(stream, tok);
        }

        public static TagTypeParserResult getTagType(TokenStream toks, Token tok) {
            final String strTok = tok.strTok;
            return switch (tok.strTok) {
                case "{" -> new TagTypeParserResult(TagTypes.COMPOUND, tok);
                case "[" -> {
                    Token arrayType = tok.next();
                    TagType type = switch (arrayType.strTok) {
                        case "B" -> TagTypes.BYTE_ARRAY;
                        case "I" -> TagTypes.INT_ARRAY;
                        case "L" -> TagTypes.LONG_ARRAY;
                        default -> TagTypes.LIST;
                    };
                    if(type == TagTypes.LIST) {
                        yield new TagTypeParserResult(type, tok);
                    }
                    yield new TagTypeParserResult(type, arrayType.next());
                }
                default -> {
                    if(strTok.equalsIgnoreCase("true") || strTok.equalsIgnoreCase("false")) {
                        yield new TagTypeParserResult(TagTypes.BYTE, tok);
                    }

                    TagType<?> type;
                    if(tok.matches(BYTE_VALUE)) {
                        type = TagTypes.BYTE;
                    } else if(tok.matches(SHORT_VALUE)) {
                        type = TagTypes.SHORT;
                    } else if(tok.matches(INT_VALUE)) {
                        type = TagTypes.INT;
                    } else if(tok.matches(LONG_VALUE)) {
                        type = TagTypes.LONG;
                    } else if(tok.matches(FLOAT_VALUE)) {
                        type = TagTypes.FLOAT;
                    } else if(tok.matches(DOUBLE_VALUE)) {
                        type = TagTypes.DOUBLE;
                    } else if(tok.matches(STRING_VALUE)) {
                        type = TagTypes.STRING;
                    } else {
                        type = null;
                        //throw new IllegalArgumentException("'" + tok + "' is not a valid tag type!");
                    }

                    yield new TagTypeParserResult(type, tok);
                }
            };
        }
    }

    public record TagTypeParserResult(TagType type, Token token) {
        // !!!! PRIORITY !!!!
        // TODO: fix TagTypeParserResult so that the token is the next token to read for the value (for compound, name. for byte/int/etc, val)

    }

    public static Tag readTag(String snbt) throws UnexpectedTokenException {
        // Format: Name:Value
        try {
            return readTag(snbt, null);
        } catch (SnbtReadException e) {
            throw new UnreachableException("Expected type is null", e);
        }
    }

    public static <T extends Tag> T readTag(String snbt, TagType<T> expectedType) throws SnbtReadException, UnexpectedTokenException {
        // Format: Name:Value
        TokenStream tokens = new TokenStream(snbt);
        Parser parser = new Parser(tokens);

        Token tok = parser.peek(); //tokens.readNextToken();
        String name = "";
        if(parser.isName(tok)) {
            name = tok.strTok;
            parser.getTokens().setPos(tok.nextStreamPos);
            parser.expect(":");
        }

        Token valToken = parser.peek();
        TagTypeParserResult result = parser.getTagType(valToken);
        if(expectedType != null && result.type != expectedType) {
            throw new SnbtReadException("Expected type " + expectedType + " for SNBT, found " + result.type);
        }
        tokens.pos = result.token().nextStreamPos;
        return (T) result.type().codec().readTag(parser, name);
    }

    public static String writeTag(Tag tag) {
        StringBuilder builder;
        if(tag.hasName()) {
            builder = new StringBuilder(tag.getName());
            builder.append(':');
        } else {
            builder = new StringBuilder();
        }

        ((TagCodec<Tag>)tag.type().codec()).writeTag(builder, tag);
        return builder.toString();
    }

    // final String test = "\"x:0\" :  {x: 0, y: 55, z: 0, Items: [{Slot: 0b, id: \"clock\", Count: 1b}, {Slot: 9b, id: \"written_book\", Count: 1b, tag: {pages: ['{\"text\":\"\\'twas brillig and the slithy toves\"}', '{\"text\":\"Did gyre and gimble in the wabe.\"}', '{\"text\":\"All mimsy were the borogoves,\"}', '{\"text\":\"And the mome raths outgrabe.\"}'], author: \"LewisCarroll\", title: \"Jabberwocky\"}}], id: \"enderchest and \"}";

    public static class Token {

        public final TokenStream stream;
        public final String strTok;
        public int nToken; // token idx
        public int nextStreamPos; // pos in stream of first char of next token

        public Token(TokenStream stream, String tok, int nToken, int nextStreamPos) {
            this.stream = stream;
            this.strTok = tok;
            this.nToken = nToken;
            this.nextStreamPos = nextStreamPos;
            System.out.println(this); // debug
        }

        public int getStreamPos() {
            return nextStreamPos - strTok.length();
        }

        public Token next() {
            return stream.readToken(nextStreamPos);
        }

        public boolean matches(String str) {
            return strTok.equals(str);
        }

        public boolean matches(Pattern regex) {
            return regex.matcher(strTok).matches();
        }

        @Override
        public String toString() {
            return "Token{" +
                    "stream=" + stream +
                    ", strTok='" + strTok + '\'' +
                    ", idx=" + nToken +
                    ", nextStreamPos=" + nextStreamPos +
                    '}';
        }
    }

    public static class TokenStream {

        private final CharSequence chars;
        private int pos;
        private int nToken;
        //private int nextIdx; // for peeking TODO: review this

        public TokenStream(CharSequence snbt) {
            this.chars = snbt;
            this.pos = 0;
        }

        // TODO: create overload for peeking tokens with offsets; NOTE: will need to be careful because this can be dangerous if done incorrectly
        public Token peekNextToken() {
            return readToken(pos);
        }

        /**
         * Read the next token from the SNBT string.
         * <p><br>
         * NOTE: returns tokens unescaped; this will return tokens copied exactly as
         * written in the SNBT string, so unescaping these strings is delegated to
         * the user.
         * </p>
         *
         * @return next token
         */
        public Token readNextToken() {
            Token tok = readToken(pos);
            this.pos = tok.nextStreamPos;
            return tok;
        }

        public Token readToken(int pos) {
            Preconditions.checkState(pos < chars.length(), "End of stream"); // TODO: return null instead of throw exception?
            int begin = pos;
            boolean isQuoted = isQuoted(chars.charAt(pos));
            do {
                char c = chars.charAt(pos);
                switch(c) {
                    case ' ' -> {
                        pos++;
                        if(!isQuoted) {
                            begin++;
                            isQuoted = isQuoted(chars.charAt(pos));
                        }
                    }
                    case '"', '\'' -> {
                        final char quote = c; // can be inlined; here for readability
                        pos++;
                        char next;
                        while((next = chars.charAt(pos)) != quote) {
                            if(next == '\\') {
                                switch (chars.charAt(++pos)) {
                                    case '\'', '\"', '\\' -> {
                                        pos++;
                                        continue;
                                    }
                                }
                            }
                            pos++;
                        }
                        String str = chars.subSequence(begin, ++pos).toString();
                        return new Token(this, str, nToken++, pos);
                    }
                    case '{', '}', ':', ',', ';', '[', ']' -> {
                        if(isQuoted) {
                            pos++;
                            continue;
                        }
                        if(pos - begin != 0) { // if not empty
                            String str = chars.subSequence(begin, pos).toString();
                            return new Token(this, str, nToken++, pos);
                        }
                        pos++;
                        String str = String.valueOf(c);
                        return new Token(this, str, nToken++, pos);
                    }
                    default -> {
                        pos++;
                    }
                }
            } while(pos < chars.length());
            throw new UnreachableException("unexpected"); // FIXME: change to SnbtReadException (this is reachable if str is malformed)
        }

        private boolean isQuoted(char c) {
            return c == '\'' || c == '"';
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        public boolean hasNext() {
            return pos < chars.length();
        }

    }

}
