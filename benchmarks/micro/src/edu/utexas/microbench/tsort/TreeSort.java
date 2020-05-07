package edu.utexas.microbench.tsort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// https://en.wikipedia.org/wiki/Tree_sort
public class TreeSort {
    private static class TreeNode<T extends Comparable<T>> {
        private TreeNode<T> left;
        private TreeNode<T> right;
        private T key;

        TreeNode(T key) {
            this.left = null;
            this.right = null;
            this.key = key;
        }

        void insert(TreeNode<T> treeNode) {
            if (treeNode.key.compareTo(key) < 0) {
                if (left != null)
                    left.insert(treeNode);
                else
                    left = treeNode;
            } else if (right != null)
                right.insert(treeNode);
            else
                right = treeNode;
        }

        void traverse(TreeVisitor<T> visitor) {
            if ( left != null)
                left.traverse( visitor );
            visitor.visit(this);
            if ( right != null )
                right.traverse( visitor );
        }

        T getKey() {
            return key;
        }
    }

    private static class TreeVisitor<T extends Comparable<T>> {
        private List<T> out = new ArrayList<>();

        void visit(TreeNode<T> node) {
            out.add(node.getKey());
        }

        List<T> getResult() {
            return out;
        }
    }

    public static <T extends Comparable<T>> List<T> sort(List<T> inputs) {
        int len = inputs.size();
        if (len == 0 || len == 1)
            return inputs;

        TreeNode<T> tree = new TreeNode<>(inputs.get(0));
        for (int i = 1; i < len; ++i)
            tree.insert(new TreeNode<>(inputs.get(i)));

        TreeVisitor<T> visitor = new TreeVisitor<>();
        tree.traverse(visitor);
        return visitor.getResult();
    }

    public static <T extends Comparable<T>> void sortArray(T arr[]) {
        List<T> outList = sort(Arrays.asList(arr));
        outList.toArray(arr);
    }

    public static void main(String args[]) {
        Integer[] inputs = new Integer[args.length];
        for (int i = 0; i < args.length; ++i)
            inputs[i] = Integer.parseInt(args[i]);
        sortArray(inputs);
        System.out.println(String.join(", ", Arrays.stream(inputs).map(Object::toString).collect(Collectors.toList())));
    }
}
