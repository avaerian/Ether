package org.minerift.ether.util.newnbt;

import org.minerift.ether.Secrets;
import org.minerift.ether.util.nbt.NBTInputStream;
import org.minerift.ether.util.newnbt.exceptions.NBTReaderException;
import org.minerift.ether.util.newnbt.tags.*;
import org.minerift.ether.util.newnbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.newnbt.tags.array.IntArrayTag;
import org.minerift.ether.util.newnbt.tags.array.LongArrayTag;
import org.minerift.ether.util.newnbt.tags.container.AbstractContainerTag;
import org.minerift.ether.util.newnbt.tags.container.CompoundTag;
import org.minerift.ether.util.newnbt.tags.container.ListTag;
import org.minerift.ether.util.newnbt.tags.container.RootEntryTag;
import org.minerift.ether.util.newnbt.token.*;
import org.minerift.ether.util.newnbt.token.dyn.CompoundChildrenContent;
import org.minerift.ether.util.newnbt.token.dyn.ExpandingTokenGroup;
import org.minerift.ether.util.newnbt.token.dyn.ListChildrenContent;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Stack;
import java.util.zip.GZIPInputStream;

public class NewNBTReader implements AutoCloseable {

    private final DataInputStream is;
    private final boolean dataStreamProvided;
    private final boolean closeResources;


    private Stack<AbstractContainerTag<?>> containerStack; // TODO: in near future, refactor to Stack of ContainerBuilders?
    private Deque<Token> tokens;

    private String currentTagName;
    private NBT.TagType currentTagType;

    /*private NewNBTReader(DataInputStream is, boolean dataStreamProvided, boolean closeResources) {
        this.is = is;
        this.dataStreamProvided = dataStreamProvided;
        this.closeResources = closeResources;
    }

    private NewNBTReader(DataInputStream is, boolean dataStreamProvided) {
        this(is, dataStreamProvided, !dataStreamProvided); // close resources if created automatically
    }

    public NewNBTReader(DataInputStream is) {
        this(is, true);
    }*/

    public NewNBTReader(File file) throws IOException {
        this(file, true);
    }

    public NewNBTReader(File file, boolean gzipped) throws IOException {
        InputStream is = new FileInputStream(file);
        if(gzipped) is = new GZIPInputStream(is);

        this.is = new DataInputStream(is);
        this.dataStreamProvided = false;
        this.closeResources = true;

        this.currentTagName = "";
        this.currentTagType = null;
        this.containerStack = new Stack<>();
        this.tokens = new ArrayDeque<>();
        pushSchemaStackLast(StatelessTokenSchema.GENERIC_TAG_ID_NAME_PAYLOAD);
    }

    private void pushSchemaStackLast(StatelessTokenSchema schema) {
        for(StatelessToken genericTok : schema.getSchema()) {
            tokens.addLast(genericTok.getToken());
        }
    }

    private void pushSchemaStackFirst(StatelessTokenSchema schema) {
        StatelessToken[] schemTokens = schema.getSchema();
        for(int i = schemTokens.length - 1; i >= 0; i--) {
            tokens.push(schemTokens[i].getToken());
        }
    }

    public void readNextToken() throws IOException {
        if(tokens.isEmpty() && !containerStack.isEmpty()) {
            pushSchemaStackLast(StatelessTokenSchema.GENERIC_TAG_ID_NAME_PAYLOAD);
        }
        readNextToken(tokens.peek());
    }

    public static void main(String[] args) throws Exception {
        File file = new File(Secrets.LOCAL_WORLD_FILE_LOC);
        NBTInputStream oldNbt = new NBTInputStream(new FileInputStream(file), true);
        NewNBTReader reader = new NewNBTReader(file);
        int largestStackSize = 0;
        try {
            while(reader.is.available() > 0) {
                reader.readNextToken();
                largestStackSize = Math.max(largestStackSize, reader.tokens.size());
            }
        } finally {
            System.out.println("containerStack: " + reader.containerStack);
            System.out.println("containerStack: " + reader.containerStack.size());
            System.out.println("largest token stack size: " + largestStackSize);
            //System.out.println("Root entry: " + reader.containerStack.getFirst().getValue());


            //System.out.println(oldNbt.readTag().getValue());
            reader.close();
        }
    }

    private Token skip() throws IOException {
        Token skipped = tokens.poll();
        System.out.println("skipped " + skipped);
        readNextToken();
        return skipped;
    }

    // TODO: move things to a NBTReaderContext? / explore the visitor pattern

    public void readNextToken(Token token) throws IOException {
    System.out.println(tokens); // debug
        switch(token) {
            case Token.TagIdToken tok -> { // entry and exit point
                byte id = tok.bytesSelected().readByte(is);
                this.currentTagType = NBT.TagType.getType(id);
                System.out.println(currentTagType); // debug
                System.out.println(currentTagType.isContainerType()); // debug

                // Close compound container if end tag is read
                if(currentTagType == NBT.TagType.END_TAG) {
                    if(containerStack.isEmpty()) {
                        throw new NBTReaderException("End tag read but no containers to close!");
                    }

                    if(containerStack.peek() instanceof RootEntryTag rootTag) {
                        tokens.clear();
                        System.out.println(rootTag.getValue());
                        // TODO

                        return;
                    }

                    if(!(containerStack.pop() instanceof CompoundTag innerTag)) {
                        throw new NBTReaderException("End tag read but no compound tag container to close");
                    }

                    //AbstractContainerTag<?> innerTag = containerStack.pop();
                    containerStack.peek().addTag(innerTag);

                    for(int i = 0; i < StatelessTokenSchema.GENERIC_TAG_ID_NAME_PAYLOAD.size(); i++) {
                        tokens.poll();
                    }
                    readNextToken();
                    return;
                }
            }

            case Token.TagNameToken tok -> {
                String tagName = tok.bytesSelected().read(is);
                this.currentTagName = tagName;
                System.out.println("tag name: " + tagName); // debug
            }

            case Token.ListChildTagIdToken tok -> {
                byte id = tok.bytesSelected().readByte(is);
                NBT.TagType childType = NBT.TagType.getType(id);
                System.out.println("list child type: " + childType); // debug
                if(!(containerStack.peek() instanceof ListTag tag)) {
                    throw new IllegalStateException("Container on top of stack is not expected ListTag!");
                }
                tag.setChildType(childType);
            }

            case Token.ListLengthToken tok -> {
                int length = tok.bytesSelected().readInt(is);
                if(!(containerStack.peek() instanceof ListTag tag)) {
                    throw new IllegalStateException("Container on top of stack is not expected ListTag!");
                }
                System.out.println("list length: " + length); // debug
                tag.setValue(new ArrayList<>(length), length);
            }

            case Token.ListStartToken tok -> {
                containerStack.push(ListTag.sizeKnownLater(currentTagName));
                this.currentTagName = ""; // reset name for list children
            }

            case Token.ListEndToken tok -> {
                if(!(containerStack.pop() instanceof ListTag tag)) {
                    throw new IllegalStateException("Container on top of stack is not expected ListTag!");
                }
                containerStack.peek().addTag(tag);
            }

            // List contents are made up of many tokens known only at runtime
            // This means that we need to iterate over the tokens in the group until we reach the end
            // NOTE: only updates tokens
            case ListChildrenContent tok -> {
                if(!tok.isReady()) {
                    ListTag tag = (ListTag) containerStack.peek(); // TODO: abstract peeking of containerStack for tokenizer ???
                    tok.setChildType(tag.getChildType());
                    tok.setListLength(tag.getCapacity());
                }

                System.out.println(tok.hasRemainingTokens()); // debug
                if(!tok.hasRemainingTokens()) {
                    skip();
                    return;
                }

                pushSchemaStackFirst(tok.readNextSchema());
                readNextToken();
                return;
            }

            // NOTE: only updates tokens
            case CompoundChildrenContent tok -> {
                if(currentTagType == NBT.TagType.END_TAG) { // last read tag type
                    skip();
                    return;
                }
                pushSchemaStackFirst(tok.readNextSchema());
                readNextToken();
                return;
            }

            case Token.CompoundStartToken tok -> {
                containerStack.push(containerStack.isEmpty()
                        ? new RootEntryTag(currentTagName)
                        : new CompoundTag(currentTagName));
            }

            case Token.CompoundEndToken tok -> {
                this.currentTagType = null; // expected to be read again next token, if any
                skip();
                return;
            }

            // NOTE: only updates tokens
            case Token.GenericTagPayloadToken tok -> {
                tokens.poll();
                pushSchemaStackFirst(currentTagType.getPayloadSchema());
                readNextToken();
                return;
            }

            case Token.TagContentByteToken tok -> {
                ByteTag tag = new ByteTag(currentTagName, tok.bytesSelected().readByte(is));
                containerStack.peek().addTag(tag);
            }

            case Token.TagContentShortToken tok -> {
                ShortTag tag = new ShortTag(currentTagName, tok.bytesSelected().readShort(is));
                containerStack.peek().addTag(tag);
            }

            case Token.TagContentIntToken tok -> {
                IntTag tag = new IntTag(currentTagName, tok.bytesSelected().readInt(is));
                containerStack.peek().addTag(tag);
            }

            case Token.TagContentLongToken tok -> {
                LongTag tag = new LongTag(currentTagName, tok.bytesSelected().readLong(is));
                containerStack.peek().addTag(tag);
            }

            case Token.TagContentStringToken tok -> {
                StringTag tag = new StringTag(currentTagName, tok.bytesSelected().read(is));
                containerStack.peek().addTag(tag);
            }

            case Token.TagContentFloatToken tok -> {
                FloatTag tag = new FloatTag(currentTagName, tok.bytesSelected().readFloat(is));
                containerStack.peek().addTag(tag);
            }

            case Token.TagContentDoubleToken tok -> {
                DoubleTag tag = new DoubleTag(currentTagName, tok.bytesSelected().readDouble(is));
                containerStack.peek().addTag(tag);
            }

            case Token.ArrayContentByteToken tok -> {
                ByteArrayTag tag = new ByteArrayTag(currentTagName, tok.bytesSelected().read(is));
                containerStack.peek().addTag(tag);
            }

            case Token.ArrayContentIntToken tok -> {
                IntArrayTag tag = new IntArrayTag(currentTagName, tok.bytesSelected().read(is));
                containerStack.peek().addTag(tag);
            }

            case Token.ArrayContentLongToken tok -> {
                LongArrayTag tag = new LongArrayTag(currentTagName, tok.bytesSelected().read(is));
                containerStack.peek().addTag(tag);
            }

            case null -> System.out.println("null token"); // TODO: log as warning
            default -> throw new IllegalStateException("Unexpected value: " + tokens.peek());
        }

        if(!(tokens.peek() instanceof ExpandingTokenGroup)) {
            System.out.println("polled: " + tokens.poll()); // TODO: remove debug
        }
    }

    @Override
    public void close() throws Exception {
        if(!dataStreamProvided || closeResources) {
            is.close();
        }
    }
}
