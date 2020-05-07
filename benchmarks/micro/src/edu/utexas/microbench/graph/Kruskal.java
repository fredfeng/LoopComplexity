package edu.utexas.microbench.graph;

import java.util.*;

public class Kruskal {

    public static List<Edge<Integer>> run(Graph<Integer> graph) {
        List<Edge<Integer>> retList = new ArrayList<>();
        List<Edge<Integer>> edgeList = graph.getEdges();
        edgeList.sort(Comparator.comparingInt(Edge::getWeight));

        int edgeCount = edgeList.size();
        int nodeCount = graph.getNumNodes();
        UF unionFind = new UF(nodeCount);
        for (int i = 0; i < edgeCount && retList.size() < nodeCount - 1; ++i) {
            Edge<Integer> currentEdge = edgeList.get(i);
            int srcRoot = unionFind.find(currentEdge.getSrc());
            int dstRoot = unionFind.find(currentEdge.getDst());
            if (srcRoot != dstRoot) {
                retList.add(currentEdge);
                unionFind.union(srcRoot, dstRoot);
            }
        }

        return retList;
    }

    // Credit: https://algs4.cs.princeton.edu/15uf/UF.java.html
    private static class UF {

        private int[] parent;  // parent[i] = parent of i
        private byte[] rank;   // rank[i] = rank of subtree rooted at i (never more than 31)
        private int count;     // number of components

        /**
         * Initializes an empty union–find data structure with {@code n} sites
         * {@code 0} through {@code n-1}. Each site is initially in its own
         * component.
         *
         * @param  n the number of sites
         * @throws IllegalArgumentException if {@code n < 0}
         */
        public UF(int n) {
            if (n < 0) throw new IllegalArgumentException();
            count = n;
            parent = new int[n];
            rank = new byte[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        /**
         * Returns the component identifier for the component containing site {@code p}.
         *
         * @param  p the integer representing one site
         * @return the component identifier for the component containing site {@code p}
         * @throws IllegalArgumentException unless {@code 0 <= p < n}
         */
        public int find(int p) {
            validate(p);
            while (p != parent[p]) {
                parent[p] = parent[parent[p]];    // path compression by halving
                p = parent[p];
            }
            return p;
        }

        /**
         * Returns the number of components.
         *
         * @return the number of components (between {@code 1} and {@code n})
         */
        public int count() {
            return count;
        }

        /**
         * Returns true if the the two sites are in the same component.
         *
         * @param  p the integer representing one site
         * @param  q the integer representing the other site
         * @return {@code true} if the two sites {@code p} and {@code q} are in the same component;
         *         {@code false} otherwise
         * @throws IllegalArgumentException unless
         *         both {@code 0 <= p < n} and {@code 0 <= q < n}
         */
        public boolean connected(int p, int q) {
            return find(p) == find(q);
        }

        /**
         * Merges the component containing site {@code p} with the
         * the component containing site {@code q}.
         *
         * @param  p the integer representing one site
         * @param  q the integer representing the other site
         * @throws IllegalArgumentException unless
         *         both {@code 0 <= p < n} and {@code 0 <= q < n}
         */
        public void union(int p, int q) {
            int rootP = find(p);
            int rootQ = find(q);
            if (rootP == rootQ) return;

            // make root of smaller rank point to root of larger rank
            if      (rank[rootP] < rank[rootQ]) parent[rootP] = rootQ;
            else if (rank[rootP] > rank[rootQ]) parent[rootQ] = rootP;
            else {
                parent[rootQ] = rootP;
                rank[rootP]++;
            }
            count--;
        }

        // validate that p is a valid index
        private void validate(int p) {
            int n = parent.length;
            if (p < 0 || p >= n) {
                throw new IllegalArgumentException("index " + p + " is not between 0 and " + (n-1));
            }
        }
    }

    public static void main(String args[]) {
        Graph<Integer> graph = Graph.createFromEdges(Arrays.asList(
                new Edge<>(0, 1, 1),
                new Edge<>(1, 2, 2),
                new Edge<>(2, 0, 1),
                new Edge<>(2, 3, 3),
                new Edge<>(3, 4, 1),
                new Edge<>(3, 5, 2),
                new Edge<>(5, 4, 1)
        ));

        List<Edge<Integer>> mstEdges = run(graph);
        for (Edge<Integer> edge: mstEdges)
            System.out.println(edge);
    }
}
