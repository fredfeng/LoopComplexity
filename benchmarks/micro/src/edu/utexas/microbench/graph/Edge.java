package edu.utexas.microbench.graph;

import java.util.Objects;

public class Edge<T> {
    private T src;
    private T dst;
    private int weight;

    public Edge(T src, T dst, int weight) {
        this.src = src;
        this.dst = dst;
        this.weight = weight;
    }

    public T getSrc() {
        return src;
    }

    public T getDst() {
        return dst;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Edge<?> edge = (Edge<?>) o;
        return weight == edge.weight &&
                Objects.equals(src, edge.src) &&
                Objects.equals(dst, edge.dst);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, dst, weight);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "src=" + src +
                ", dst=" + dst +
                ", weight=" + weight +
                '}';
    }
}
