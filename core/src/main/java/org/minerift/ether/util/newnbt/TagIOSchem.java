package org.minerift.ether.util.newnbt;

import org.minerift.ether.Secrets;
import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.util.newnbt.tags.Tag;
import org.minerift.ether.util.newnbt.tags.container.CollectionTagBuilder;
import org.minerift.ether.util.newnbt.token.Token;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;
import java.util.zip.GZIPInputStream;

// Represents a queue of tokens ("data types") to read/write
public class TagIOSchem {


    // TODO: for State, maybe use Stack of TagBuilders? for reading recursive entries in a state machine
    public static class State {
        private NBT.TagType currentTagType;
        // complexTags stores a stack of
        private Stack<CollectionTagBuilder<Tag>> complexTags; // values are accumulated in the tag builders
        private Stack<Tag> simpleTags; // simple tags are accumulated here for collection-type tags
        private Deque<Token> knownTokens;
        public Token currentToken, nextToken;
        private DataInputStream is; // TODO: remove after testing (refactor appropriately)

        public State() {
            this.currentTagType = null;
            this.complexTags = new Stack<>();
            this.currentToken = Token.TAG_ID; // start by reading tag id
            this.nextToken = null;
        }

        // TODO:



        public void readNext() {
            try {
                switch(currentToken) {
                    case Token.TagIdToken tok -> {
                        byte tagId = tok.bytesSelected().readByte(is);
                        currentTagType = NBT.TagType.getType(tagId);
                    }

                    case Token.ArrayContentByteToken tok -> {
                        CollectionTagBuilder<?> complexTag = complexTags.peek();
                        byte[] content = tok.bytesSelected().read(is, complexTag.getCapacity());
                        // TODO: write content to complexTag

                        complexTags.pop();
                    }

                    /*case TagNameContentToken tok -> {

                    }

                    case StringContentToken tok -> {
                    }*/

                    default -> {}
                }
            } catch (IOException ignored) {

            }



            currentToken = nextToken;
        }

        public IBuilder<Tag> peekCurrentTag() {
            return complexTags.peek();
        }

        public IBuilder<Tag> popCurrentTag() {
            return complexTags.pop();
        }

    }

    public static void main(String[] args) throws IOException {

        File file = new File(Secrets.LOCAL_SCHEM_FILE_LOC, "test_schem1.schem");
        Deque<Token> tokens = new ArrayDeque<>();
        tokens.push(Token.TAG_ID);
        try(DataInputStream is = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {

            /*switch(tokens.peek()) {
                case TagIdToken tok -> {
                    byte id = tok.bytesSelected().readByte(is);
                    NBT.TagType tagType = NBT.TagType.getType(id);
                }
                case null -> {}
                default -> throw new IllegalStateException("Unexpected value: " + tokens.peek());
            }*/


            NBT.TagType tagType = NBT.TagType.getType(Token.TAG_ID.bytesSelected().readByte(is));
            String name = Token.TAG_NAME.bytesSelected().read(is);
            System.out.println(name + " is a " + tagType);
            System.out.println("Next tag is a " + NBT.TagType.getType(Token.TAG_ID.bytesSelected().readByte(is)));
            System.out.println(Token.TAG_NAME.bytesSelected().read(is));

            Stack<Integer> test = new Stack<>();
            test.push(1);
            test.push(2);
            test.push(420);
            test.push(69);
            System.out.println(test.get(test.size() - 2));

            // TAG_ID + TAG_NAME + LIST_CHILD_TYPE_ID + LIST_LENGTH + LIST_START + TAG_CONTENT_STRING + TAG_CONTENT_STRING + LIST_END (no io primitive)
            // TAG_ID + TAG_NAME + LIST_CHILD_TYPE_ID + LIST_LENGTH + LIST_START + LIST_CHILDREN_CONTENT

            // read TAG_ID and push new ListTag onto containerStack
            // read TAG_NAME and set currentTagName
            // load io schem based off TAG_ID (in this case, for ListTag)

            // LIST_CHILD_TYPE_ID -> read child type id and get current list tag on top of stack and set type
            // LIST_LENGTH -> read list length and get current list tag on top of stack and set length

            // LIST_START -> set name of currentTagName to ""
            // anytime a TAG_CONTENT value is read, a new tag will be pushed to the stack with the currentTagName and the read value

            // TAG_CONTENT_STRING -> push onto scalarStack
            // TAG_CONTENT_STRING -> push onto scalarStack

            // LIST_END
            // read current list tag on top of stack
            // pop list length number of tags and add those to list tag on top of containerStack
            // pop tag from containerStack and add to next one on containerStack

        }
    }

    //public static class LazyRepeatedTokenGroup implements LazyTokenGroup {
    //}

    /*public record TagStartToken() implements Token {
        @Override
        public String getName() {
            return "TAG_START_TOKEN";
        }
    }*/


}
