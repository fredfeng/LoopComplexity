package edu.utexas.microbench.graph;

import java.util.*;
import java.util.stream.Collectors;

public class EdgeSet<T> implements Iterable<Edge<T>> {
    private Map<T, Edge<T>> theMap = new HashMap<>();

    public int size() {
        return theMap.size();
    }

    public boolean isEmpty() {
        return theMap.isEmpty();
    }

    public boolean add(Edge<T> edge) {
        return theMap.putIfAbsent(edge.getDst(), edge) == null;
    }

    public boolean containsDst(T dst) {
        return theMap.containsKey(dst);
    }

    public boolean contains(Edge<T> edge) {
        Edge<T> theEdge = theMap.get(edge.getDst());
        return theEdge != null && theEdge.equals(edge);
    }

    @Override
    public Iterator<Edge<T>> iterator() {
        return theMap.values().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EdgeSet<?> edgeSet = (EdgeSet<?>) o;
        return Objects.equals(theMap, edgeSet.theMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(theMap);
    }

    @Override
    public String toString() {
        return "{" +
                String.join(", ", theMap.values().stream().map(Edge::toString).collect(Collectors.toList())) +
                '}';
    }
}
