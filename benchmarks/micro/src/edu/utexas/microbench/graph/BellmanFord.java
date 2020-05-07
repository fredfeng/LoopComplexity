package edu.utexas.microbench.graph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BellmanFord {

    // Return null if the graph has negative cycle
    public static <T> Map<T, Integer> run(Graph<T> graph, T source) {
        Map<T, Integer> spMap = new HashMap<>();

        for (T node: graph)
            spMap.put(node, Integer.MAX_VALUE);

        spMap.put(source, 0);
        for (int i = 0; i < graph.getNumNodes(); ++i) {
            boolean changed = false;
            for (Edge<T> edge: graph.getEdges()) {
                int distu = spMap.get(edge.getSrc());
                int distv = spMap.get(edge.getDst());
                int distNew = distu + edge.getWeight();
                if (distv > distNew) {
                    spMap.put(edge.getDst(), distNew);
                    changed = true;
                }
            }
            if (!changed)
                break;
        }

        for (Edge<T> edge: graph.getEdges()) {
            int distu = spMap.get(edge.getSrc());
            int distv = spMap.get(edge.getDst());
            if (distv > distu + edge.getWeight())
                return null;
        }

        return spMap;
    }

    public static Map<Integer, Integer> runIntGraph(Graph<Integer> graph, Integer source) {
        return run(graph, source);
    }

    public static void main(String args[]) {
        Graph<Integer> graph = Graph.createFromEdges(Arrays.asList(
                new Edge<>(0, 1, 2),
                new Edge<>(1, 2, 3),
                new Edge<>(2, 3, 4)
        ));

        Integer source = 0;
        Map<Integer, Integer> spMap = runIntGraph(graph, source);
        System.out.println("Source = " + source);
        System.out.println("Graph = " + graph);
        for (Map.Entry<Integer, Integer> entry: spMap.entrySet()) {
            System.out.println("SP of " + entry.getKey() + " = " + entry.getValue());
        }
    }
}
