package edu.utexas.microbench.graph;

import java.util.*;
import java.util.stream.Collectors;

public class Graph<T> implements Iterable<T> {
    private Map<T, EdgeSet<T>> nodeMap = new HashMap<>();

    public void addNode(T node) {
        nodeMap.computeIfAbsent(node, n -> new EdgeSet<>());
    }

    public boolean hasNode(T node) {
        return nodeMap.containsKey(node);
    }

    public void addEdge(T src, T dst, int weight) {
        addNode(src);
        addNode(dst);
        Edge<T> edge = new Edge<>(src, dst, weight);
        nodeMap.get(src).add(edge);
    }

    public EdgeSet<T> getAdjacentEdges(T node) {
        return nodeMap.get(node);
    }

    public int getNumNodes() {
        return nodeMap.size();
    }

    public int getNumEdges() {
        int total = 0;
        for (EdgeSet<T> edges: nodeMap.values()) {
            total += edges.size();
        }
        return total;
    }

    public List<Edge<T>> getEdges() {
        List<Edge<T>> edgeList = new ArrayList<>();
        for (EdgeSet<T> edges: nodeMap.values()) {
            for (Edge<T> edge: edges)
                edgeList.add(edge);
        }
        return edgeList;
    }

    public static <T> Graph<T> createFromEdges(List<Edge<T>> edges) {
        Graph<T> graph = new Graph<>();
        for (Edge<T> edge: edges) {
            graph.addEdge(edge.getSrc(), edge.getDst(), edge.getWeight());
        }
        return graph;
    }

    public static <T> Graph<T> createFromEdgeArray(Edge<T> edges[]) {
        return createFromEdges(Arrays.asList(edges));
    }

    public static <T> Graph<T> create(List<T> nodes, List<Edge<T>> edges) {
        Graph<T> graph = new Graph<>();
        for (T node: nodes)
            graph.addNode(node);
        for (Edge<T> edge: edges)
            graph.addEdge(edge.getSrc(), edge.getDst(), edge.getWeight());
        return graph;
    }

    public static <T> Graph<T> createFromArrays(T nodes[], Edge<T> edges[]) {
        return create(Arrays.asList(nodes), Arrays.asList(edges));
    }

    @Override
    public Iterator<T> iterator() {
        return nodeMap.keySet().iterator();
    }

    @Override
    public String toString() {
        return "Graph{" +
                String.join(", ", nodeMap.values().stream().map(EdgeSet::toString).collect(Collectors.toList())) +
                '}';
    }
}
