package org.minerift.ether.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class CmdTree {

    public static void main(String[] args) {
        // testing ground
        RootCmdNode root = new RootCmdNode("island", "is");

        SubCmdListNode cmds = new SubCmdListNode();
        cmds.addCmd(new SubCmdNode("create"));
        cmds.addCmd(new SubCmdNode("home"));
        cmds.addCmd(new SubCmdNode("sethome"));
        cmds.addCmd(new SubCmdNode(""));

        root.attach(cmds);
    }

    public interface CmdNode {
        CmdNode attach(CmdNode node);
    }

    public interface OptionalCmdNode {

    }

    //public interface SubCmdsNode {}

    public static class SubCmdListNode implements CmdNode/*, SubCmdsNode*/ {
        private Map<String, SubCmdNode> cmds;

        public SubCmdListNode() {
            this.cmds = new HashMap<>();
        }

        public void addCmd(SubCmdNode node) {
            cmds.put(node.name, node);
        }

        public SubCmdNode getCmd(String name) {
            return cmds.get(name);
        }

        @Override
        public SubCmdListNode attach(CmdNode node) {
            if(!(node instanceof SubCmdNode)) {
                throw new IllegalArgumentException();
            }
            addCmd((SubCmdNode) node);
            return this;
        }
    }

    public static class SubCmdNode implements CmdNode, OptionalCmdNode {
        public String name;
        public CmdNode node;

        public SubCmdNode(String name) {
            this.name = name;
        }

        @Override
        public SubCmdNode attach(CmdNode node) {
            this.node = node;
            return this;
        }
    }

    public static class RootCmdNode implements CmdNode {
        public String name;
        public String[] aliases;
        public CmdNode node; // could be arg, sub cmd, sub cmds, or exec node

        public RootCmdNode(String name, String...aliases) {
            this.name = name;
            this.aliases = aliases;
            this.node = null;
            //this.node = childNode;
        }

        @Override
        public RootCmdNode attach(CmdNode node) {
            this.node = node;
            return this;
        }
    }

    // CmdNode: SubCmdListNode OR CmdArgNode

    // CmdNode -> List<CmdNode> OR CmdArgNode ????

    public static class ArgCmdNode<T> implements CmdNode, OptionalCmdNode {

        private CmdNode node;
        private final Supplier<List<String>> options;
        private final Function<String, T> nameToTypeFn;

        public ArgCmdNode(Supplier<List<String>> options, Function<String, T> nameToTypeFn) {
            this.options = options;
            this.nameToTypeFn = nameToTypeFn;
        }

        public List<String> getOptions() {
            return options.get();
        }

        public T getOption(String name) {
            return nameToTypeFn.apply(name);
        }

        @Override
        public ArgCmdNode<T> attach(CmdNode node) {
            this.node = node;
            return this;
        }
    }

    public static class ExecCmdNode implements CmdNode {

        @Override
        public ExecCmdNode attach(CmdNode node) {
            throw new UnsupportedOperationException();
        }
    }

}
