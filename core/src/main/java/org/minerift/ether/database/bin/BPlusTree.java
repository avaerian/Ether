package org.minerift.ether.database.bin;

import org.minerift.ether.debug.Debug;

import java.nio.ByteBuffer;
import java.util.LinkedList;

@Deprecated
public class BPlusTree<K extends Comparable<K>, V> { // NOTE: Key is K, Value is pointer to record in buffer/disk

    /* When writing tree, store:
        - (chunk info, which will already be handled by the chunk system)
        - tree (index) name,
        - ...

       If when writing the data is larger than the chunk/buffer, allocate another chunk connected to the previous and continue writing data
       Before writing tree, collect references to all nodes in a list and then read/serialize backwards

       ~~L2R and R2L serializers will be available to write in either direction~~
       ~~L2R deserializer will be the only option (unless needed)~~

       - Chunks can probably be stored in a timed cache map??
    */

    private int degree; // TODO: figure out how we should calculate it
    private TreeNode<K, V> root;



    public BPlusTree() {
        this.degree = 2;
        this.root = null;
    }

    public void insert(K key, V val) {
        if(isEmpty()) {
            this.root = new TreeInternalNode<>(degree);
        }


    }

    public void insertNonFull() {
        ByteBuffer test = null;
        //test.put()
    }

    public TreeLeaf<K, V> findLeaf(K key) {

        if(isEmpty()) {
            return null;
        }

        TreeNode<K, V> node = root;


        return null;
    }

    @Debug
    public static void main(String[] args) {
        BPlusTree<Integer, Integer> tree = new BPlusTree<>();
        tree.root = new TreeInternalNode<>(10);
        tree.root.currentCapacity = 5;
        tree.root.keys = new Integer[]{0, 5, 10, 21, 69};
        Bound bound = Bound.PLUS_ONE;
        for(int key : tree.root.keys) {
            System.out.println(key - 1 + ": " + tree.binSearch(tree.root, key - 1, bound));
            System.out.println(key + ": " + tree.binSearch(tree.root, key, bound));
            System.out.println(key + 1 + ": " + tree.binSearch(tree.root, key + 1, bound));
        }
    }

    private int binSearch(TreeNode<K, V> node, K key) {
        return binSearch(node, key, Bound.EXACT);
    }

    // Return index in node that key belongs to.
    // Bound still needs to be fully implemented, but works for what is needed for now
    // https://github.com/andylamp/BPlusTree/blob/7911be7e6606df45ccd981ed685ab5f5afbd7cba/src/main/java/ds/bplus/bptree/BPlusTree.java#L376
    private int binSearch(TreeNode<K, V> node, K key, Bound bound) {
        int l = 0;
        int r = node.currentCapacity - 1;
        while(l <= r) {
            int idx = (l + r) / 2;
            K mkey = node.keys[idx];
            int cmp = mkey.compareTo(key);

            if(cmp < 0) { // left
                l = idx + 1;
            } else if(cmp > 0) { // right
                r = idx - 1;
            } else { // equal
                return bound == Bound.PLUS_ONE ? idx + 1 : idx;
            }
        }
        return l;
    }

    private enum Bound { LOWER, UPPER, PLUS_ONE, EXACT }

    // Returns proper address, or null if not exists
    public V findAddress(K key) {

        if(isEmpty()) {
            return null;
        }




        return null;
    }

    public boolean isEmpty() {
        return root == null;
    }


    public static abstract class TreeNode<K, V> {

        // Order N: each internal node has between N and N*2 data entries

        protected K[] keys;
        protected TreeNode<K, V>[] children;
        protected int currentCapacity; // for keys; children = currentCapacity + 1


        public TreeNode(int degree) {
            this.keys = (K[]) new Object[degree * 2];
            this.children = new TreeNode[(degree * 2) + 1];
            this.currentCapacity = 0;
        }

    }

    public static class TreeInternalNode<K, V> extends TreeNode<K, V> {

        public TreeInternalNode(int degree) {
            super(degree);
        }
    }

    public static class TreeLeaf<K, V> extends TreeNode<K, V> {
        protected TreeLeaf<K, V> nextLeaf;
        protected TreeLeaf<K, V> prevLeaf;
        protected LinkedList<V> values; // pointers to records on disk


        public TreeLeaf(int degree) {
            super(degree);
        }
    }

}
