package com.roboticcusp.mapping;

/**
 * Created by rborbely on 10/13/16.
 */
public class EulerianAlg {

    public static boolean isEulerian(Chart graph) throws ChartException {
        ConnectedAlg ca = new ConnectedAlg();
        return ca.isConnected(graph) && !graph.hasOddDegree();
    }
}
